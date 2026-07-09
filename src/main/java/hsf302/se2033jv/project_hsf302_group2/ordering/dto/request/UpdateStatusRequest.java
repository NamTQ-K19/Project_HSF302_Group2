package hsf302.se2033jv.project_hsf302_group2.ordering.dto.request;

import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    private OrderStatus status;
    private String note;
}
