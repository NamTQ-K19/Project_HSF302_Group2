package hsf302.se2033jv.project_hsf302_group2.admin.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataResponse {
    private List<String> labels;
    private List<BigDecimal> salesData;
    private List<Integer> orderCounts;
}