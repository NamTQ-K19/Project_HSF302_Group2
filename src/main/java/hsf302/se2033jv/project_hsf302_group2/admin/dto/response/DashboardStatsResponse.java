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
public class DashboardStatsResponse {
    private SalesStats salesStats;
    private List<RecentOrder> recentOrders;
    private List<InventoryAlert> inventoryAlerts;
    private List<AIInsight> aiInsights;
    private SystemStatus systemStatus;
    private StaffStats staffStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesStats {
        private BigDecimal totalSalesToday;
        private BigDecimal salesChangePercent;
        private Long ordersCompleted;
        private Double ordersChangePercent;
        private String topSellingItem;
        private Integer topSellingItemCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private String customerName;
        private String itemName;
        private String size;
        private String time;
        private String status;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryAlert {
        private String name;
        private String detail;
        private String status; // CRITICAL, LOW, MEDIUM
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIInsight {
        private String title;
        private String impact; // HIGH, MEDIUM, LOW
        private String impactValue;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemStatus {
        private String status;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffStats {
        private Integer activeStaff;
        private String status;
    }
}