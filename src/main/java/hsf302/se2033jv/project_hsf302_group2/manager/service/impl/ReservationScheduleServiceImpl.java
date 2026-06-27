package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Reservation;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ReservationDeposit;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.DepositPaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import hsf302.se2033jv.project_hsf302_group2.common.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.EmailService;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ReservationScheduleFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationDetailResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationScheduleResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationStatsResponse;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ReservationScheduleRepository;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ReservationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduleServiceImpl implements ReservationScheduleService {

    private final ReservationScheduleRepository reservationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public Page<ReservationScheduleResponse> getSchedule(ReservationScheduleFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // 1. Chuyển đổi trạng thái từ String sang Enum (nếu có)
        ReservationStatus statusEnum = null;
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            try {
                statusEnum = ReservationStatus.fromValue(filter.getStatus()); // Hoặc ReservationStatus.valueOf(...) tùy Enum của bạn
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: " + filter.getStatus());
            }
        }

        // 2. Gọi 1 hàm duy nhất, Database sẽ tự động lo việc lọc các biến null
        Page<Reservation> reservationPage = reservationRepository.findWithDynamicFilter(
                filter.getFromDate(),
                filter.getToDate(),
                statusEnum,
                pageable
        );

        // 3. Map sang Response
        List<ReservationScheduleResponse> responseList = reservationPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, reservationPage.getTotalElements());
    }

    @Override
    public ReservationScheduleResponse getDetail(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Đặt bàn không tồn tại"));
        return mapToResponse(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Integer reservationId, String reason, Integer cancelledByUserId) {
        List<ReservationStatus> cancellableStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        Reservation reservation = reservationRepository.findByReservationIdAndStatusIn(reservationId, cancellableStatuses)
                .orElseThrow(() -> new RuntimeException("CANNOT_CANCEL_RESERVATION"));

        User manager = userRepository.findById(cancelledByUserId)
                .orElseThrow(() -> new RuntimeException("Manager không tồn tại"));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancellationReason(reason);
        reservation.setCancelledBy(manager);
        reservation.setCancelledAt(LocalDateTime.now());

        reservationRepository.save(reservation);

        // Gửi email (không throw nếu lỗi)
        try {
            String customerEmail = reservation.getCustomer().getEmail();
            String customerName = reservation.getCustomer().getFirstName() + " " + reservation.getCustomer().getLastName();
            emailService.sendReservationCancellationEmail(customerEmail, customerName,
                    reservation.getReservationDate(), reservation.getReservationTime(), reason);
        } catch (Exception e) {
            log.warn("Không thể gửi email thông báo hủy cho khách: " + e.getMessage());
        }
    }

    @Override
    public ReservationStatsResponse getStats(LocalDate fromDate, LocalDate toDate) {
        // 1. Đếm tổng số theo ngày (Bỏ qua biến Trạng thái của form)
        long totalReservations = reservationRepository.countWithDynamicDates(fromDate, toDate);

        // 2. Đếm các trạng thái cụ thể theo ngày
        long pendingCount = reservationRepository.countWithDynamicDatesAndStatus(fromDate, toDate, ReservationStatus.PENDING);
        long confirmedCount = reservationRepository.countWithDynamicDatesAndStatus(fromDate, toDate, ReservationStatus.CONFIRMED);
        long arrivedCount = reservationRepository.countWithDynamicDatesAndStatus(fromDate, toDate, ReservationStatus.ARRIVED);
        long completedCount = reservationRepository.countWithDynamicDatesAndStatus(fromDate, toDate, ReservationStatus.COMPLETED);
        long cancelledCount = reservationRepository.countWithDynamicDatesAndStatus(fromDate, toDate, ReservationStatus.CANCELLED);
        long noShowCount = reservationRepository.countWithDynamicDatesAndStatus(fromDate, toDate, ReservationStatus.NO_SHOW);

        // 3. Tính tỷ lệ cọc thanh toán theo ngày
        List<Reservation> allReservations = reservationRepository.findReservationsForStats(fromDate, toDate);
        long totalWithDeposit = allReservations.stream()
                .filter(r -> r.getDeposits() != null && !r.getDeposits().isEmpty())
                .count();

        long paidDepositCount = allReservations.stream()
                .flatMap(r -> r.getDeposits() != null ? r.getDeposits().stream() : java.util.stream.Stream.empty())
                .filter(d -> DepositPaymentStatus.PAID.equals(d.getPaymentStatus()))
                .count();

        double depositPaymentRate = totalWithDeposit > 0 ? (double) paidDepositCount / totalWithDeposit * 100 : 0;

        return ReservationStatsResponse.builder()
                .totalReservations(totalReservations)
                .pendingCount(pendingCount)
                .confirmedCount(confirmedCount)
                .arrivedCount(arrivedCount)
                .completedCount(completedCount)
                .cancelledCount(cancelledCount)
                .noShowCount(noShowCount)
                .paidDepositCount(paidDepositCount)
                .totalWithDeposit(totalWithDeposit)
                .depositPaymentRate(Math.round(depositPaymentRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public ReservationDetailResponse getReservationDetail(Integer reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));

        // 1. Xử lý Bàn
        List<ReservationDetailResponse.TableInfo> tableInfos = r.getTables().stream()
                .map(t -> new ReservationDetailResponse.TableInfo(t.getTableId(), t.getCapacity()))
                .toList();

        // 2. Xử lý Tiền cọc
        String depositStatus = "Chưa cọc";
        BigDecimal depositAmount = null;
        if (r.getDeposits() != null && !r.getDeposits().isEmpty()) {
            var deposit = r.getDeposits().get(0);
            depositAmount = deposit.getDepositAmount();
            depositStatus = "PAID".equals(deposit.getPaymentStatus().name()) ? "Đã thanh toán" : "Đang chờ";
        }

        // 3. Xử lý Người hủy
        String cancelledByName = null;
        String cancelledByRole = null; // Biến mới

        if (r.getCancelledBy() != null) {
            cancelledByName = r.getCancelledBy().getFirstName() + " " + r.getCancelledBy().getLastName();

            // Lấy Role và dịch sang tiếng Việt cho đẹp giao diện
            if (r.getCancelledBy().getRole() != null) {
                String roleCode = r.getCancelledBy().getRole().getRoleName().toUpperCase();
                cancelledByRole = switch (roleCode) {
                    case "ADMIN" -> "Quản trị viên";
                    case "MANAGER" -> "Quản lý";
                    case "STAFF" -> "Nhân viên";
                    case "CUSTOMER", "USER" -> "Khách hàng";
                    default -> roleCode;
                };
            } else {
                cancelledByRole = "Khách hàng";
            }
        }

        // 4. Map vào DTO
        return ReservationDetailResponse.builder()
                .reservationId(r.getReservationId())
                .customerName(r.getCustomer().getFirstName() + " " + r.getCustomer().getLastName())
                .customerPhone(r.getCustomer().getPhone())
                .customerEmail(r.getCustomer().getEmail())
                .username(r.getCustomer().getUsername())
                .reservationDate(r.getReservationDate())
                .reservationTime(r.getReservationTime())
                .durationMinutes(r.getDurationMinutes())
                .partySize(r.getPartySize())
                .note(r.getNote())
                .status(r.getStatus())
                .statusLabel(getStatusLabel(r.getStatus()))
                .depositStatus(depositStatus)
                .depositAmount(depositAmount)
                .assignedTables(tableInfos)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .cancelledByName(cancelledByName)
                .cancelledByRole(cancelledByRole)
                .cancellationReason(r.getCancellationReason())
                .cancelledAt(r.getCancelledAt())
                .orderId(r.getOrder() != null ? r.getOrder().getOrderId() : null)
                .build();
    }

    private ReservationScheduleResponse mapToResponse(Reservation reservation) {
        String tableNumbers = reservation.getTables() != null
                ? reservation.getTables().stream()
                    .map(table -> "T" + String.format("%02d", table.getTableId()))
                    .sorted()
                    .collect(Collectors.joining(", "))
                : "";

        String depositStatus = "Không có cọc";
        BigDecimal depositAmount = null;

        if (reservation.getDeposits() != null && !reservation.getDeposits().isEmpty()) {
            ReservationDeposit deposit = reservation.getDeposits().get(0);
            depositAmount = deposit.getDepositAmount();
            if (DepositPaymentStatus.PAID.equals(deposit.getPaymentStatus())) {
                depositStatus = "Đã cọc";
            } else if (DepositPaymentStatus.PENDING.equals(deposit.getPaymentStatus())) {
                depositStatus = "Chưa cọc";
            }
        }

        return ReservationScheduleResponse.builder()
                .reservationId(reservation.getReservationId())
                .customerName(reservation.getCustomer().getFirstName() + " " + reservation.getCustomer().getLastName())
                .customerPhone(reservation.getCustomer().getPhone())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .durationMinutes(reservation.getDurationMinutes())
                .partySize(reservation.getPartySize())
                .tableNumbers(tableNumbers)
                .status(reservation.getStatus())
                .statusLabel(getStatusLabel(reservation.getStatus()))
                .depositStatus(depositStatus)
                .depositAmount(depositAmount)
                .note(reservation.getNote())
                .build();
    }

    private String getStatusLabel(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case ARRIVED -> "Đã đến";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            case NO_SHOW -> "Không đến";
            default -> status.getValue();
        };
    }
}

