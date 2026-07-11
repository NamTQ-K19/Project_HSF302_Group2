package hsf302.se2033jv.project_hsf302_group2.cashier.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.request.CashierReservationFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.request.CashierRefundRequest;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierReservationListResponse;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierReservationDetailResponse;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierRefundResponse;
import hsf302.se2033jv.project_hsf302_group2.cashier.service.interfaces.ReservationTableService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.*;
import hsf302.se2033jv.project_hsf302_group2.common.enums.DepositPaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.RefundStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.*;
import hsf302.se2033jv.project_hsf302_group2.common.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTableServiceImpl implements ReservationTableService {

    private final ReservationRepository reservationRepository;
    private final ReservationDepositRepository depositRepository;
    private final UserRepository userRepository;
    private final SystemLogRepository systemLogRepository;
    private final ObjectMapper objectMapper;
    private final hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.EmailService emailService;

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Override
    public Page<CashierReservationListResponse> getReservations(CashierReservationFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Reservation> reservations = reservationRepository.searchReservationsForCashier(
                filter.getKeyword(),
                filter.getDate(),
                pageable
        );

        return reservations.map(this::mapToListResponse);
    }

    @Override
    public CashierReservationDetailResponse getReservationDetail(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt bàn với ID: " + reservationId));

        ReservationDeposit deposit = depositRepository.findByReservationReservationId(reservationId)
                .orElse(null);

        List<Integer> tableIds = reservation.getTables().stream()
                .map(CoffeeTable::getTableId)
                .collect(Collectors.toList());

        String tablesDisplay = reservation.getTables().stream()
                .map(t -> "Bàn " + t.getTableId())
                .collect(Collectors.joining(", "));

        String cancelledByName = "";
        if (reservation.getCancelledBy() != null) {
            User cb = reservation.getCancelledBy();
            cancelledByName = ((cb.getFirstName() != null ? cb.getFirstName() : "") + " " +
                    (cb.getLastName() != null ? cb.getLastName() : "")).trim();
            if (cancelledByName.isEmpty()) {
                cancelledByName = cb.getUsername();
            }
        }

        String transactionRefDisplay = "";
        String paymentMethodName = "Chưa thanh toán";
        String depositStatus = "PENDING";
        String depositStatusDisplay = "Chưa cọc";
        BigDecimal depositAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        String refundStatusVal = "NONE";
        String refundStatusDisplay = "Chưa hoàn";
        String refundNote = "";
        Integer depositId = null;

        if (deposit != null) {
            depositId = deposit.getDepositId();
            depositAmount = deposit.getDepositAmount();
            depositStatus = deposit.getPaymentStatus() != null ? deposit.getPaymentStatus().name() : "PENDING";
            depositStatusDisplay = getDepositStatusVi(deposit.getPaymentStatus());
            
            String rawRef = deposit.getTransactionRef();
            transactionRefDisplay = (rawRef != null && rawRef.contains("|")) ? rawRef.split("\\|")[0] : rawRef;
            
            if (deposit.getPaymentMethod() != null) {
                paymentMethodName = deposit.getPaymentMethod().getName();
            }
            
            refundAmount = deposit.getRefundAmount() != null ? deposit.getRefundAmount() : BigDecimal.ZERO;
            refundStatusVal = deposit.getRefundStatus() != null ? deposit.getRefundStatus().name() : "NONE";
            refundStatusDisplay = getRefundStatusVi(deposit.getRefundStatus());
            refundNote = deposit.getRefundNote();
        }

        return CashierReservationDetailResponse.builder()
                .reservationId(reservation.getReservationId())
                .customerId(reservation.getCustomer().getUserId())
                .customerName((reservation.getCustomer().getFirstName() + " " + reservation.getCustomer().getLastName()).trim())
                .customerEmail(reservation.getCustomer().getEmail())
                .customerPhone(reservation.getCustomer().getPhone())
                .partySize(reservation.getPartySize())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .durationMinutes(reservation.getDurationMinutes())
                .status(reservation.getStatus())
                .statusDisplay(getStatusVi(reservation.getStatus()))
                .note(reservation.getNote())
                .cancellationReason(reservation.getCancellationReason())
                .cancelledAt(reservation.getCancelledAt())
                .cancelledByName(cancelledByName)
                .createdAt(reservation.getCreatedAt())
                .tableIds(tableIds)
                .tablesDisplay(tablesDisplay)
                .depositId(depositId)
                .depositAmount(depositAmount)
                .depositPaymentStatus(depositStatus)
                .depositPaymentStatusDisplay(depositStatusDisplay)
                .transactionRef(transactionRefDisplay)
                .paymentMethodName(paymentMethodName)
                .refundAmount(refundAmount)
                .refundStatus(refundStatusVal)
                .refundStatusDisplay(refundStatusDisplay)
                .refundNote(refundNote)
                .build();
    }

    @Override
    @Transactional
    public CashierRefundResponse processRefund(CashierRefundRequest request, String cashierUsername, String ipAddress) {
        log.info("Processing refund request for reservation: {}, amount: {} by cashier: {}", 
                request.getReservationId(), request.getAmount(), cashierUsername);

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt bàn ID: " + request.getReservationId()));

        ReservationDeposit deposit = depositRepository.findByReservationReservationId(request.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tiền cọc của đặt bàn này"));

        if (deposit.getPaymentStatus() != DepositPaymentStatus.PAID) {
            return CashierRefundResponse.builder()
                    .success(false)
                    .message("Không thể hoàn tiền cho giao dịch cọc ở trạng thái: " + getDepositStatusVi(deposit.getPaymentStatus()))
                    .build();
        }

        if (request.getAmount().compareTo(deposit.getDepositAmount()) > 0) {
            return CashierRefundResponse.builder()
                    .success(false)
                    .message("Số tiền hoàn không được lớn hơn số tiền cọc: " + deposit.getDepositAmount() + " ₫")
                    .build();
        }

        PaymentMethod pm = deposit.getPaymentMethod();
        String paymentMethodName = pm != null ? pm.getName() : "";
        boolean isVNPay = "VNPay".equalsIgnoreCase(paymentMethodName);

        User cashier = userRepository.findByUsername(cashierUsername).orElse(null);

        if (isVNPay) {
            // HƯỚNG 1: Xử lý qua cổng VNPay Refund API
            String rawRef = deposit.getTransactionRef();
            if (rawRef == null || !rawRef.contains("|")) {
                return CashierRefundResponse.builder()
                        .success(false)
                        .message("Thông tin giao dịch cọc VNPAY không hợp lệ hoặc thiếu dữ liệu đối soát (vnp_TxnRef/PayDate)")
                        .build();
            }

            String[] parts = rawRef.split("\\|");
            String vnpTransactionNo = parts[0];
            String vnpTxnRef = parts.length > 1 ? parts[1] : "";
            String vnpPayDate = parts.length > 2 ? parts[2] : "";

            if (vnpTxnRef.isEmpty() || vnpPayDate.isEmpty()) {
                return CashierRefundResponse.builder()
                        .success(false)
                        .message("Thiếu mã giao dịch hoặc ngày thanh toán VNPAY gốc để thực hiện hoàn tiền")
                        .build();
            }

            try {
                // Chuẩn bị tham số gọi API VNPay
                String requestId = UUID.randomUUID().toString().replace("-", "");
                String version = "2.1.0";
                String command = "refund";
                String txnType = request.getAmount().compareTo(deposit.getDepositAmount()) == 0 ? "02" : "03"; // 02: Hoàn toàn phần, 03: Hoàn một phần
                long amountCents = request.getAmount().multiply(new BigDecimal("100")).longValue();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                String createDateStr = formatter.format(new Date());

                // Tạo chữ ký SecureHash
                String hashData = requestId + "|" + version + "|" + command + "|" + vnpTmnCode + "|" + 
                        txnType + "|" + vnpTxnRef + "|" + amountCents + "|" + vnpTransactionNo + "|" + 
                        vnpPayDate + "|" + cashierUsername + "|" + createDateStr + "|" + ipAddress + "|" + request.getNote();
                
                String secureHash = VNPayUtil.hmacSHA512(vnpHashSecret, hashData);

                // Build Body JSON
                Map<String, Object> reqBody = new LinkedHashMap<>();
                reqBody.put("vnp_RequestId", requestId);
                reqBody.put("vnp_Version", version);
                reqBody.put("vnp_Command", command);
                reqBody.put("vnp_TmnCode", vnpTmnCode);
                reqBody.put("vnp_TransactionType", txnType);
                reqBody.put("vnp_TxnRef", vnpTxnRef);
                reqBody.put("vnp_Amount", amountCents);
                reqBody.put("vnp_TransactionNo", vnpTransactionNo);
                reqBody.put("vnp_TransactionDate", vnpPayDate);
                reqBody.put("vnp_CreateBy", cashierUsername);
                reqBody.put("vnp_CreateDate", createDateStr);
                reqBody.put("vnp_IpAddr", ipAddress);
                reqBody.put("vnp_OrderInfo", request.getNote() != null ? request.getNote() : "Hoan tien coc dat ban " + reservation.getReservationId());
                reqBody.put("vnp_SecureHash", secureHash);

                String jsonPayload = objectMapper.writeValueAsString(reqBody);
                log.info("Sending VNPAY Refund Payload: {}", jsonPayload);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                String responseBody = httpResponse.body();
                log.info("VNPAY Refund API Response: {}", responseBody);

                Map<String, Object> respMap = objectMapper.readValue(responseBody, Map.class);
                String responseCode = (String) respMap.get("vnp_ResponseCode");
                String responseMsg = (String) respMap.get("vnp_Message");

                if ("00".equals(responseCode)) {
                    // Cập nhật Database khi hoàn tiền thành công
                    updateDepositRefundSuccess(deposit, request.getAmount(), request.getNote(), cashier);
                    logSystemAction(cashier, "REFUND_DEPOSIT_VNPAY", "reservation_deposits", deposit.getDepositId(), 
                            "Hoàn tiền cọc VNPay thành công: " + request.getAmount() + " ₫. Lý do: " + request.getNote());
                    
                    return CashierRefundResponse.builder()
                            .success(true)
                            .message("Hoàn tiền qua VNPay thành công!")
                            .transactionRef(vnpTransactionNo)
                            .build();
                } else {
                    log.error("VNPAY Refund failed with code: {}, msg: {}", responseCode, responseMsg);
                    
                    // Đối với môi trường Demo Sandbox, giao dịch test có thể hết hạn hoặc không tồn tại trên hệ thống VNPay.
                    // Ta vẫn hiển thị thông báo lỗi từ VNPay để đúng quy trình, nhưng hỗ trợ tùy chọn báo lỗi rõ ràng.
                    return CashierRefundResponse.builder()
                            .success(false)
                            .message("Cổng VNPAY từ chối giao dịch hoàn tiền. Mã lỗi: " + responseCode + " - " + responseMsg)
                            .build();
                }
            } catch (Exception e) {
                log.error("Error communicating with VNPay Refund API", e);
                return CashierRefundResponse.builder()
                        .success(false)
                        .message("Lỗi kết nối cổng thanh toán VNPay: " + e.getMessage())
                        .build();
            }
        } else {
            // HƯỚNG 2: Các phương thức thanh toán còn lại (Tiền mặt, chuyển khoản...) -> Auto thành công
            updateDepositRefundSuccess(deposit, request.getAmount(), request.getNote(), cashier);
            logSystemAction(cashier, "REFUND_DEPOSIT_MANUAL", "reservation_deposits", deposit.getDepositId(), 
                    "Hoàn tiền cọc thủ công (" + paymentMethodName + ") thành công: " + request.getAmount() + " ₫. Lý do: " + request.getNote());

            return CashierRefundResponse.builder()
                    .success(true)
                    .message("Hoàn tiền cọc (" + paymentMethodName + ") thành công (Hệ thống tự động duyệt)!")
                    .transactionRef(deposit.getTransactionRef())
                    .build();
        }
    }

    private void updateDepositRefundSuccess(ReservationDeposit deposit, BigDecimal amount, String note, User cashier) {
        deposit.setRefundAmount(amount);
        deposit.setRefundNote(note);
        deposit.setPaymentStatus(DepositPaymentStatus.REFUNDED);
        if (amount.compareTo(deposit.getDepositAmount()) == 0) {
            deposit.setRefundStatus(RefundStatus.FULL);
        } else {
            deposit.setRefundStatus(RefundStatus.PARTIAL);
        }
        depositRepository.save(deposit);

        // Đồng thời cập nhật trạng thái của Reservation thành CANCELLED
        Reservation reservation = deposit.getReservation();
        if (reservation != null) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setCancelledBy(cashier);
            reservation.setCancelledAt(java.time.LocalDateTime.now());
            reservation.setCancellationReason("Thu ngân hoàn tiền cọc: " + note);
            if (reservation.getTables() != null) {
                reservation.getTables().clear();
            }
            reservationRepository.save(reservation);

            // Gửi email thông báo hủy bàn cho khách hàng
            try {
                if (reservation.getCustomer() != null && reservation.getCustomer().getEmail() != null) {
                    emailService.sendReservationCancellationEmail(
                            reservation.getCustomer().getEmail(),
                            (reservation.getCustomer().getFirstName() + " " + reservation.getCustomer().getLastName()).trim(),
                            reservation.getReservationDate(),
                            reservation.getReservationTime(),
                            "Thu ngân hoàn tiền cọc đặt bàn và hủy bàn. Lý do: " + note
                    );
                    log.info("Sent cancellation email to customer {} for reservation {}", reservation.getCustomer().getEmail(), reservation.getReservationId());
                }
            } catch (Exception e) {
                log.error("Failed to send cancellation email for reservation: " + reservation.getReservationId(), e);
            }
        }
    }

    private CashierReservationListResponse mapToListResponse(Reservation r) {
        ReservationDeposit deposit = depositRepository.findByReservationReservationId(r.getReservationId())
                .orElse(null);

        String paymentMethodName = "Chưa cọc";
        String depositStatus = "PENDING";
        String depositStatusDisplay = "Chưa cọc";
        BigDecimal depositAmount = BigDecimal.ZERO;
        String refundStatusVal = "NONE";

        if (deposit != null) {
            depositAmount = deposit.getDepositAmount();
            depositStatus = deposit.getPaymentStatus() != null ? deposit.getPaymentStatus().name() : "PENDING";
            depositStatusDisplay = getDepositStatusVi(deposit.getPaymentStatus());
            if (deposit.getPaymentMethod() != null) {
                paymentMethodName = deposit.getPaymentMethod().getName();
            }
            refundStatusVal = deposit.getRefundStatus() != null ? deposit.getRefundStatus().name() : "NONE";
        }

        String displayPhone = r.getCustomer().getPhone();
        if (displayPhone == null || displayPhone.isEmpty()) {
            displayPhone = "N/A";
        }

        return CashierReservationListResponse.builder()
                .reservationId(r.getReservationId())
                .customerName((r.getCustomer().getFirstName() + " " + r.getCustomer().getLastName()).trim())
                .customerPhone(displayPhone)
                .partySize(r.getPartySize())
                .reservationDate(r.getReservationDate())
                .reservationTime(r.getReservationTime())
                .durationMinutes(r.getDurationMinutes())
                .status(r.getStatus())
                .statusDisplay(getStatusVi(r.getStatus()))
                .depositAmount(depositAmount)
                .depositPaymentStatus(depositStatus)
                .depositPaymentStatusDisplay(depositStatusDisplay)
                .paymentMethodName(paymentMethodName)
                .createdAt(r.getCreatedAt())
                .build();
    }

    private String getStatusVi(ReservationStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING:   return "Chờ thanh toán";
            case CONFIRMED: return "Đã xác nhận";
            case ARRIVED:   return "Đã đến";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            case NO_SHOW:   return "Không đến";
            default:        return status.name();
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
            default:        return status.name();
        }
    }

    private String getRefundStatusVi(RefundStatus status) {
        if (status == null) return "Chưa hoàn";
        switch (status) {
            case NONE:    return "Chưa hoàn";
            case PARTIAL: return "Hoàn một phần";
            case FULL:    return "Hoàn toàn phần";
            default:      return status.name();
        }
    }

    private void logSystemAction(User user, String action, String targetType, Integer targetId, String description) {
        SystemLog log = SystemLog.builder()
                .user(user)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .build();
        systemLogRepository.save(log);
    }
}
