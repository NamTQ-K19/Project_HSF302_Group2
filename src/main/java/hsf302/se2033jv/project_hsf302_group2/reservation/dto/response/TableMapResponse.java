package hsf302.se2033jv.project_hsf302_group2.reservation.dto.response;

import hsf302.se2033jv.project_hsf302_group2.common.enums.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMapResponse {
    private Integer tableId;
    private Integer capacity;
    private Boolean isAvailable;
    private String tableName;
}