package hsf302.se2033jv.project_hsf302_group2.ordering.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TablePosResponse {
    private Integer tableId;
    private Integer capacity;
    private String status;
    private Boolean isActive;
}
