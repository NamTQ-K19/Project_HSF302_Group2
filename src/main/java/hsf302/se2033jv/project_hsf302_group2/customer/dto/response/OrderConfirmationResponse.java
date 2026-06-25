// customer/dto/response/OrderConfirmationResponse.java
package hsf302.se2033jv.project_hsf302_group2.customer.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderConfirmationResponse {
    private Integer orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String transactionRef;
    private Integer pointsEarned;
    private Integer totalPoints;
    private LocalDateTime estimatedTime;
    private String message;
}