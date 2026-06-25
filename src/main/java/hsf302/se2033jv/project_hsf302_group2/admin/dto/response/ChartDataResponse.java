// D:\SU26\HSF302\Practice\Project_HSF302_Group2\src\main\java\hsf302\se2033jv\project_hsf302_group2\admin\dto\response\ChartDataResponse.java
package hsf302.se2033jv.project_hsf302_group2.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataResponse {
    private List<String> labels;
    private List<BigDecimal> salesData;
    private List<Integer> orderCounts;
}