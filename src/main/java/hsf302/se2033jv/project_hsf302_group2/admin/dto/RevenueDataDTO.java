package hsf302.se2033jv.project_hsf302_group2.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDataDTO {
    private BigDecimal totalRevenue;
    private int totalOrders;
    // Map with Key = Month, Value = Revenue
    private Map<Integer, BigDecimal> revenueByMonth;
    // Map with Key = Product Name, Value = Quantity Sold
    private Map<String, Long> topSellingProducts;
}
