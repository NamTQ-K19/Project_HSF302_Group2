package hsf302.se2033jv.project_hsf302_group2.ordering.dto.request;

import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Integer customerId;
    private Integer tableId;
    private OrderType orderType;
    private String note;
    private Integer paymentMethodId;
    private Boolean isPaidImmediately;
    private Boolean usePoints;
    private Integer pointsToUse;
    private List<OrderItemRequest> items;
}
