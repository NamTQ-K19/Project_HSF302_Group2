package hsf302.se2033jv.project_hsf302_group2.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    // Thông tin đơn hàng
    private Integer orderId;
    private String orderTypeLabel;     // "Tại quầy" / "Online"
    private String tableNumber;        // null nếu online
    private LocalDateTime orderDate;
    private String customerName;
    private String customerEmail;      // để hiện nút gửi email hay không

    // Danh sách món
    private List<InvoiceItemResponse> items;

    // Tổng kết
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private Integer pointsEarned;

    // Thanh toán
    private String paymentMethodName;
    private String paymentStatusLabel; // "Đã thanh toán"
    private String transactionRef;     // null nếu là tiền mặt
    private LocalDateTime paidAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemResponse {
        private String productName;
        private String variantName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal itemTotal;
        private String specialNote;
    }
}
