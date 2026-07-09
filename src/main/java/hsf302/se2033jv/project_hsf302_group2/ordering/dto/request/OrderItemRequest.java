package hsf302.se2033jv.project_hsf302_group2.ordering.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private Integer productId;
    private Integer variantId;
    private Integer quantity;
    private String specialNote;
}
