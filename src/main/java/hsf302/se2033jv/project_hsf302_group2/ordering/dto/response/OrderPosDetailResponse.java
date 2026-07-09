package hsf302.se2033jv.project_hsf302_group2.ordering.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPosDetailResponse {
    private Integer orderId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Integer tableId;
    private String orderType;
    private String orderStatus;
    private String paymentStatus;
    private Integer paymentMethodId;
    private String paymentMethodName;
    private String transactionRef;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private Integer pointsEarned;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String note;
    private List<OrderItemPosResponse> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemPosResponse {
        private Integer itemId;
        private Integer productId;
        private String productName;
        private String variantName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal itemTotal;
        private String specialNote;
    }
}
