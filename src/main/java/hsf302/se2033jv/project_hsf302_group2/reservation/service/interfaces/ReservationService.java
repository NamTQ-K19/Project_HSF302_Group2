package hsf302.se2033jv.project_hsf302_group2.reservation.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CancelReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {

    // Lấy danh sách đặt bàn của customer, mới nhất lên đầu
    List<ReservationResponse> getMyReservations(Integer customerId);

    // Lấy một đặt bàn để hiển thị trang xác nhận hủy
    // Ném RuntimeException nếu không tìm thấy hoặc không thuộc customer này
    ReservationResponse getReservationForCancel(Integer reservationId, Integer customerId);

    // Thực hiện hủy đặt bàn (PENDING/CONFIRMED mới được hủy)
    // Ném RuntimeException nếu không hợp lệ
    void cancelReservation(CancelReservationRequest request, Integer customerId);
}
