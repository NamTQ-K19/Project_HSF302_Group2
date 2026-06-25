package hsf302.se2033jv.project_hsf302_group2.reservation.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CoffeeTable;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Reservation;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ReservationDeposit;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.DepositPaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.TableStatus;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CancelReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.response.ReservationResponse;
import hsf302.se2033jv.project_hsf302_group2.reservation.repository.ReservationDepositRepository;
import hsf302.se2033jv.project_hsf302_group2.reservation.repository.ReservationRepository;
import hsf302.se2033jv.project_hsf302_group2.reservation.service.interfaces.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus.CANCELLED;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationDepositRepository reservationDepositRepository;

    @Override
    public List<ReservationResponse> getMyReservations(Integer customerId) {
        return reservationRepository
                .findByCustomer_UserIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationResponse getReservationForCancel(Integer reservationId, Integer customerId) {
        Reservation reservation = reservationRepository
                .findByReservationIdAndCustomer_UserId(reservationId, customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));
        return mapToDTO(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(CancelReservationRequest request, Integer customerId) {

        // 1. Kiểm tra quyền sở hữu
        Reservation reservation = reservationRepository
                .findByReservationIdAndCustomer_UserId(request.getReservationId(), customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));

        // 2. Kiểm tra trạng thái
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Không thể hủy đặt bàn ở trạng thái: "
                    + reservation.getStatus().getValue());
        }

        // 3. Xử lý deposit nếu có
        reservationDepositRepository
                .findByReservation_ReservationId(reservation.getReservationId())
                .ifPresent(deposit -> {
                    if (deposit.getPaymentStatus() == DepositPaymentStatus.PAID) {
                        deposit.setPaymentStatus(DepositPaymentStatus.FORFEITED);
                    } else {
                        deposit.setPaymentStatus(DepositPaymentStatus.CANCELLED);
                    }
                    reservationDepositRepository.save(deposit);
                });

        // 4. Cập nhật reservation
        reservation.setStatus(CANCELLED);
        reservation.setCancelledBy(User.builder().userId(customerId).build());
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(request.getCancellationReason());
        reservationRepository.save(reservation);

        // 5. Giải phóng bàn
        if (reservation.getTables() != null) {
            reservation.getTables().forEach(table ->
                    table.setStatus(TableStatus.AVAILABLE)
            );
        }
    }

    // mapToDTO dùng setter — KHÔNG dùng builder()
    private ReservationResponse mapToDTO(Reservation r) {
        ReservationDeposit deposit = reservationDepositRepository
                .findByReservation_ReservationId(r.getReservationId())
                .orElse(null);

        ReservationResponse dto = new ReservationResponse();
        dto.setReservationId(r.getReservationId());
        dto.setReservationDate(r.getReservationDate());
        dto.setReservationTime(r.getReservationTime());
        dto.setPartySize(r.getPartySize());
        dto.setDurationMinutes(r.getDurationMinutes());
        dto.setStatus(r.getStatus());
        dto.setStatusVi(getStatusVi(r.getStatus()));
        dto.setNote(r.getNote());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setCanCancel(r.getStatus() == ReservationStatus.PENDING
                || r.getStatus() == ReservationStatus.CONFIRMED);

        if (deposit != null) {
            dto.setDepositAmount(deposit.getDepositAmount());
            dto.setDepositStatus(deposit.getPaymentStatus());
            dto.setDepositStatusVi(getDepositStatusVi(deposit.getPaymentStatus()));
        }

        if (r.getTables() != null) {
            dto.setTableIds(r.getTables().stream()
                    .map(CoffeeTable::getTableId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private String getStatusVi(ReservationStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING:   return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case ARRIVED:   return "Đã đến";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            case NO_SHOW:   return "Không đến";
            default:        return status.getValue();
        }
    }

    private String getDepositStatusVi(DepositPaymentStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING:   return "Chưa thanh toán";
            case PAID:      return "Đã thanh toán";
            case REFUNDED:  return "Đã hoàn tiền";
            case CANCELLED: return "Đã hủy";
            case FORFEITED: return "Mất cọc";
            default:        return status.getValue();
        }
    }
}
