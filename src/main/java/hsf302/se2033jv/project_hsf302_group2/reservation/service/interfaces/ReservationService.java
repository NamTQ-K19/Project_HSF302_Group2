package hsf302.se2033jv.project_hsf302_group2.reservation.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.entity.MapEntity;
import hsf302.se2033jv.project_hsf302_group2.common.entity.PaymentMethod;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Reservation;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CancelReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CreateReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.DepositPaymentRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.TableAvailabilityRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {

    // Lấy danh sách đặt bàn của customer, mới nhất lên đầu
    List<ReservationResponse> getMyReservations(Integer customerId);

    // Lấy danh sách đặt bàn của customer có phân trang, mới nhất lên đầu
    Page<ReservationResponse> getMyReservations(Integer customerId, Pageable pageable);

    // Lấy một đặt bàn để hiển thị trang xác nhận hủy
    // Ném RuntimeException nếu không tìm thấy hoặc không thuộc customer này
    ReservationResponse getReservationForCancel(Integer reservationId, Integer customerId);

    // Thực hiện hủy đặt bàn (PENDING/CONFIRMED mới được hủy)
    // Ném RuntimeException nếu không hợp lệ
    void cancelReservation(CancelReservationRequest request, Integer customerId);

    // Kiểm tra bàn trống
    TableAvailabilityResponse checkAvailability(TableAvailabilityRequest request);

    // Tạo đặt bàn mới
    ReservationConfirmationResponse createReservation(CreateReservationRequest request, Integer customerId);

    // Lấy chi tiết đặt bàn
    MakeReservationResponse getReservationDetail(Integer reservationId, Integer customerId);

    // Thanh toán tiền cọc
    ReservationConfirmationResponse payDeposit(DepositPaymentRequest request, Integer customerId);

    // Xác nhận đặt bàn
    MakeReservationResponse confirmReservation(Integer reservationId);

    // Giải phóng bàn hết hạn
    void releaseExpiredHolds();

    // Kiểm tra trạng thái đặt bàn
    MakeReservationResponse getReservationStatus(Integer reservationId);

    // Lấy danh sách bàn cho map
    List<TableMapResponse> getTablesForMap(LocalDate date, LocalTime time, Integer partySize);

    // Lấy tất cả map
    List<MapEntity> getAllMaps();

    //Lấy phương thức thanh toán
    List<PaymentMethod> getAllPaymentMethods();

    // THÊM MỚI: Xử lý kết quả thanh toán từ VNPay (GIỐNG ORDER)/
    void handleGatewayPaymentResult(Integer reservationId, boolean success, String transactionRef, String rawResponse);

    // Xử lý kết quả thanh toán từ VNPay (trả về Reservation)/
    Reservation handleVNPayPaymentResult(Integer reservationId, String vnpTxnRef, boolean success);

    // THÊM MỚI: Lấy Reservation theo ID
    Reservation getReservationById(Integer reservationId);

    ReservationDepositInfo getDepositByReservationId(Integer reservationId);
}
