package hsf302.se2033jv.project_hsf302_group2.reservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableAvailabilityResponse {

    private Boolean available;
    private String message;
    private List<TableInfo> availableTables;
    private List<TableInfo> suggestedTables;
    private Integer totalAvailable;
    private Integer requiredCapacity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableInfo {
        private Integer tableId;
        private Integer capacity;
        private String status;
    }
}