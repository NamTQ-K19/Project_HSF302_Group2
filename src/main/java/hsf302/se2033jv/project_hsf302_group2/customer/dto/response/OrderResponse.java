// customer/dto/response/OrderResponse.java
package hsf302.se2033jv.project_hsf302_group2.customer.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Integer orderId;
    private String orderNumber;
    private String orderStatus;
    private String orderType;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private Integer pointsEarned;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean fullyReviewed;
    private List<OrderItemResponse> items;
    private DeliveryAddressResponse deliveryAddress;
    private PaymentResponse payment;

    @Data
    public static class OrderItemResponse {
        private Integer itemId;
        private String productName;
        private String variantName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal itemTotal;
        private String specialNote;
        private String itemStatus;
        private String productImage;
        private Boolean reviewed;
    }

    @Data
    public static class DeliveryAddressResponse {
        private Integer addressId;
        private String label;
        private String fullAddress;
        private String recipientName;
        private String recipientPhone;
    }

    @Data
    public static class PaymentResponse {
        private Integer paymentId;
        private String paymentMethod;
        private BigDecimal amount;
        private String paymentStatus;
        private String transactionRef;
        private LocalDateTime paidAt;
    }
}