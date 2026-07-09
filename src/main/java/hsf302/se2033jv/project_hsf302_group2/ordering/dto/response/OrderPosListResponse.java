package hsf302.se2033jv.project_hsf302_group2.ordering.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPosListResponse {
    private Integer orderId;
    private String customerName;
    private String customerPhone;
    private Integer tableId;
    private String orderType;
    private String orderStatus;
    private String paymentStatus;
    private String paymentMethodName;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String note;
}
