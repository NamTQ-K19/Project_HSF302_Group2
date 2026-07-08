package hsf302.se2033jv.project_hsf302_group2.payment.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Payment;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PaymentRepository;
import hsf302.se2033jv.project_hsf302_group2.common.util.VNPayUtil;
import hsf302.se2033jv.project_hsf302_group2.payment.dto.request.VNPayReturnResultRequest;
import hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final PaymentRepository paymentRepository;

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    @Value("${vnpay.version}")
    private String vnpVersion;

    @Value("${vnpay.command}")
    private String vnpCommand;

    @Override
    public String createPaymentUrl(Order order, HttpServletRequest request) {
        String vnpTxnRef = order.getOrderId() + "_" + System.currentTimeMillis();
        long amount = order.getTotalAmount().longValue() * 100; // VNPay yêu cầu x100

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnpVersion);
        params.put("vnp_Command", vnpCommand);
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpReturnUrl);
        params.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String name = itr.next();
            String value = params.get(name);
            if (value != null && !value.isEmpty()) {
                hashData.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(name, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (itr.hasNext()) { hashData.append('&'); query.append('&'); }
            }
        }

        String secureHash = VNPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        // Lưu vnpTxnRef vào Payment để đối soát khi VNPay redirect về
        Payment payment = paymentRepository.findByOrder_OrderId(order.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order " + order.getOrderId()));
        payment.setTransactionRef(vnpTxnRef);
        payment.setPaymentStatus(PaymentStatus.PENDING); // reset PENDING (hỗ trợ cả trường hợp retry)
        paymentRepository.save(payment);

        log.info("Created VNPay payment URL for order {} with txnRef {}", order.getOrderId(), vnpTxnRef);
        return vnpPayUrl + "?" + query;
    }

    @Override
    public VNPayReturnResultRequest processReturn(Map<String, String> params) {
        Map<String, String> fields = new HashMap<>(params);
        String receivedHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String name = itr.next();
            String value = fields.get(name);
            if (value != null && !value.isEmpty()) {
                hashData.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (itr.hasNext()) hashData.append('&');
            }
        }

        String calculatedHash = VNPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());
        boolean valid = calculatedHash.equalsIgnoreCase(receivedHash);

        String vnpTxnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String vnpTransactionNo = params.get("vnp_TransactionNo");

        Integer orderId = null;
        try {
            orderId = Integer.parseInt(vnpTxnRef.split("_")[0]);
        } catch (Exception e) {
            log.warn("Cannot parse orderId from vnpTxnRef: {}", vnpTxnRef);
        }

        boolean success = valid && "00".equals(responseCode);
        String message = !valid ? "Sai checksum, dữ liệu có thể đã bị giả mạo"
                : (success ? "Thanh toán thành công" : "Giao dịch không thành công, mã lỗi: " + responseCode);

        return VNPayReturnResultRequest.builder()
                .valid(valid)
                .success(success)
                .orderId(orderId)
                .vnpTxnRef(vnpTxnRef)
                .vnpTransactionNo(vnpTransactionNo)
                .message(message)
                .build();
    }
}