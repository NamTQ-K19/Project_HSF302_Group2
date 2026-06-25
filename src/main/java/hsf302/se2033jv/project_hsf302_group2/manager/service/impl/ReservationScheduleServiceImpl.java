package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Reservation;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ReservationDeposit;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.DepositPaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import hsf302.se2033jv.project_hsf302_group2.customer.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ReservationScheduleFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationScheduleResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationStatsResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.repository.ReservationScheduleRepository;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ManagerEmailService;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ReservationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final ManagerEmailService emailService;

    @Override
    public Page<ReservationScheduleResponse> getSchedule(ReservationScheduleFilterRequest filter, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        Page<Reservation> reservationPage;

        // If no date filter, get all reservations
        if (filter.getFromDate() == null || filter.getToDate() == null) {
            reservationPage = reservationRepository.findAllByOrderByReservationDateDescReservationTimeAsc(pageable);
        } else if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            try {
                ReservationStatus status = ReservationStatus.fromValue(filter.getStatus());
                reservationPage = reservationRepository
                        .findByReservationDateBetweenAndStatusOrderByReservationTimeAsc(
                                filter.getFromDate(), filter.getToDate(), status, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: " + filter.getStatus());
                reservationPage = reservationRepository
                        .findByReservationDateBetweenOrderByReservationTimeAsc(
                                filter.getFromDate(), filter.getToDate(), pageable);
            }
        } else {
            reservationPage = reservationRepository
                    .findByReservationDateBetweenOrderByReservationTimeAsc(
                            filter.getFromDate(), filter.getToDate(), pageable);
        }

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
        long totalReservations = reservationRepository.countByReservationDateBetween(fromDate, toDate);

        long pendingCount = reservationRepository.countByReservationDateBetweenAndStatus(fromDate, toDate, ReservationStatus.PENDING);
        long confirmedCount = reservationRepository.countByReservationDateBetweenAndStatus(fromDate, toDate, ReservationStatus.CONFIRMED);
        long arrivedCount = reservationRepository.countByReservationDateBetweenAndStatus(fromDate, toDate, ReservationStatus.ARRIVED);
        long completedCount = reservationRepository.countByReservationDateBetweenAndStatus(fromDate, toDate, ReservationStatus.COMPLETED);
        long cancelledCount = reservationRepository.countByReservationDateBetweenAndStatus(fromDate, toDate, ReservationStatus.CANCELLED);
        long noShowCount = reservationRepository.countByReservationDateBetweenAndStatus(fromDate, toDate, ReservationStatus.NO_SHOW);

        // Tính tỷ lệ cọc thanh toán từ deposits
        List<Reservation> allReservations = reservationRepository.findByReservationDateBetweenOrderByReservationTimeAsc(fromDate, toDate);
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
    public ReservationStatsResponse getStatsAll() {
        long totalReservations = reservationRepository.count();

        long pendingCount = reservationRepository.countByStatus(ReservationStatus.PENDING);
        long confirmedCount = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        long arrivedCount = reservationRepository.countByStatus(ReservationStatus.ARRIVED);
        long completedCount = reservationRepository.countByStatus(ReservationStatus.COMPLETED);
        long cancelledCount = reservationRepository.countByStatus(ReservationStatus.CANCELLED);
        long noShowCount = reservationRepository.countByStatus(ReservationStatus.NO_SHOW);

        // Tính tỷ lệ cọc thanh toán từ deposits (tất cả)
        List<Reservation> allReservations = reservationRepository.findAll();
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

