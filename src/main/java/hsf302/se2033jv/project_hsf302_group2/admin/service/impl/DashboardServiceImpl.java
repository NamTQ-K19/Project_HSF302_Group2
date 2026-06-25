package hsf302.se2033jv.project_hsf302_group2.admin.service.impl;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.ChartDataResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.DashboardStatsResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.RoleStatsDTO;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.SystemLogDTO;
import hsf302.se2033jv.project_hsf302_group2.admin.repository.OrderRepository;
import hsf302.se2033jv.project_hsf302_group2.admin.repository.SystemLogRepository;
import hsf302.se2033jv.project_hsf302_group2.admin.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.DashboardService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SystemLogRepository systemLogRepository;

    // ===== GIỮ LẠI (ĐÃ CÓ SẴN) =====

    @Override
    public DashboardStatsResponse getDashboardStats() {
        log.info("Fetching dashboard statistics");

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayStart.minusSeconds(1);

        BigDecimal todaySales = orderRepository.sumCompletedOrderAmountsSince(todayStart);
        Long todayOrders = orderRepository.countCompletedOrdersSince(todayStart);
        BigDecimal yesterdaySales = orderRepository.sumCompletedOrderAmountsBetween(yesterdayStart, yesterdayEnd);
        Long yesterdayOrders = orderRepository.countCompletedOrdersBetween(yesterdayStart, yesterdayEnd);

        BigDecimal salesChange = calculatePercentageChange(todaySales, yesterdaySales);
        Double ordersChange = calculatePercentageChangeDouble(todayOrders.doubleValue(), yesterdayOrders.doubleValue());

        List<Object[]> topItems = orderRepository.findTopSellingItemsSince(todayStart);
        String topItemName = "N/A";
        Integer topItemCount = 0;
        if (!topItems.isEmpty()) {
            Object[] top = topItems.get(0);
            topItemName = (String) top[0];
            topItemCount = ((Number) top[1]).intValue();
        }

        Long activeStaff = userRepository.countActiveStaff();

        List<Order> recentOrders = orderRepository.findRecentOrdersWithStatus();
        List<DashboardStatsResponse.RecentOrder> recentOrderDTOs = recentOrders.stream()
                .limit(8)
                .map(this::convertToRecentOrder)
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .salesStats(DashboardStatsResponse.SalesStats.builder()
                        .totalSalesToday(todaySales)
                        .salesChangePercent(salesChange)
                        .ordersCompleted(todayOrders)
                        .ordersChangePercent(ordersChange)
                        .topSellingItem(topItemName)
                        .topSellingItemCount(topItemCount)
                        .build())
                .recentOrders(recentOrderDTOs)
                .inventoryAlerts(buildInventoryAlerts())
                .aiInsights(buildAIInsights())
                .systemStatus(DashboardStatsResponse.SystemStatus.builder()
                        .status("OPERATIONAL")
                        .message("All systems operational")
                        .build())
                .staffStats(DashboardStatsResponse.StaffStats.builder()
                        .activeStaff(activeStaff != null ? activeStaff.intValue() : 0)
                        .status("All present")
                        .build())
                .build();
    }

    @Override
    public ChartDataResponse getChartData() {
        log.info("Fetching chart data");

        LocalDateTime startDate = LocalDateTime.now().minusDays(6);
        List<Object[]> results = orderRepository.getDailySalesStats(startDate);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> salesData = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();

        Map<String, Object[]> dataMap = new HashMap<>();
        for (Object[] row : results) {
            dataMap.put((String) row[0], row);
        }

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.toString();
            String label = date.format(DateTimeFormatter.ofPattern("dd/MM"));
            labels.add(label);

            if (dataMap.containsKey(dateStr)) {
                Object[] row = dataMap.get(dateStr);
                salesData.add((BigDecimal) row[1]);
                orderCounts.add(((Number) row[2]).intValue());
            } else {
                salesData.add(BigDecimal.ZERO);
                orderCounts.add(0);
            }
        }

        return ChartDataResponse.builder()
                .labels(labels)
                .salesData(salesData)
                .orderCounts(orderCounts)
                .build();
    }

    @Override
    public long getTotalUsers() {
        return userRepository.countTotalUsers();
    }

    @Override
    public long getNewUsersToday() {
        return userRepository.countUsersCreatedAfter(LocalDate.now().atStartOfDay());
    }

    @Override
    public long getActiveUsers() {
        return userRepository.countActiveUsers();
    }

    @Override
    public long getLockedUsers() {
        return userRepository.countLockedUsers();
    }

    @Override
    public List<RoleStatsDTO> getRoleStats() {
        List<Map<String, Object>> roleData = userRepository.countUsersByRole();
        List<RoleStatsDTO> result = new ArrayList<>();

        if (roleData != null && !roleData.isEmpty()) {
            long total = roleData.stream().mapToLong(m -> (Long) m.get("count")).sum();
            for (Map<String, Object> row : roleData) {
                String roleName = (String) row.get("roleName");
                Long count = (Long) row.get("count");
                Double percentage = total > 0 ? (count.doubleValue() / total) * 100 : 0.0;
                result.add(RoleStatsDTO.builder()
                        .roleName(roleName != null ? roleName : "UNKNOWN")
                        .count(count)
                        .percentage(Math.round(percentage * 10.0) / 10.0)
                        .build());
            }
        }
        return result;
    }

    @Override
    public List<SystemLogDTO> getRecentLogs() {
        List<SystemLog> logs = systemLogRepository.findTop5ByOrderByCreatedAtDesc();
        List<SystemLogDTO> result = new ArrayList<>();

        if (logs != null && !logs.isEmpty()) {
            for (SystemLog log : logs) {
                String actionType = log.getAction() != null ? log.getAction().toUpperCase() : "INFO";
                String userName = log.getUser() != null ?
                        log.getUser().getFirstName() + " " + log.getUser().getLastName() : "Hệ thống";
                String createdAt = log.getCreatedAt() != null ?
                        log.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")) : "N/A";

                result.add(SystemLogDTO.builder()
                        .actionType(actionType)
                        .description(log.getDescription() != null ? log.getDescription() : "Không có mô tả")
                        .userName(userName)
                        .createdAt(createdAt)
                        .build());
            }
        }
        return result;
    }

    private DashboardStatsResponse.RecentOrder convertToRecentOrder(Order order) {
        String customerName = "Guest";
        if (order.getUser() != null) {
            customerName = order.getUser().getFirstName() + " " + order.getUser().getLastName();
        }

        String itemName = "N/A";
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            OrderDetail firstDetail = order.getOrderDetails().get(0);
            itemName = firstDetail.getProductNameSnapshot() != null ? firstDetail.getProductNameSnapshot() : "Product";
        }

        String size = "N/A";
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            OrderDetail firstDetail = order.getOrderDetails().get(0);
            if (firstDetail.getVariant() != null && firstDetail.getVariant().getSize() != null) {
                size = firstDetail.getVariant().getSize().name();
            }
        }

        String time = order.getCreatedAt() != null ?
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("h:mm a")) : "N/A";

        String status = order.getOrderStatus() != null ? order.getOrderStatus().name() : "PENDING";

        return DashboardStatsResponse.RecentOrder.builder()
                .customerName(customerName)
                .itemName(itemName)
                .size(size)
                .time(time)
                .status(status)
                .amount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .build();
    }

    private BigDecimal calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        if (current == null) {
            return BigDecimal.valueOf(-100);
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private Double calculatePercentageChangeDouble(Double current, Double previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        return Double.parseDouble(String.format("%.1f", ((current - previous) / previous) * 100));
    }

    private List<DashboardStatsResponse.InventoryAlert> buildInventoryAlerts() {
        List<DashboardStatsResponse.InventoryAlert> alerts = new ArrayList<>();
        alerts.add(DashboardStatsResponse.InventoryAlert.builder()
                .name("Oat Milk")
                .detail("2 liters left (5%)")
                .status("CRITICAL")
                .build());
        alerts.add(DashboardStatsResponse.InventoryAlert.builder()
                .name("Colombian Beans")
                .detail("5 kg left (20%)")
                .status("LOW")
                .build());
        alerts.add(DashboardStatsResponse.InventoryAlert.builder()
                .name("Vanilla Syrup")
                .detail("3 bottles left (20%)")
                .status("MEDIUM")
                .build());
        alerts.add(DashboardStatsResponse.InventoryAlert.builder()
                .name("Paper Cups (Large)")
                .detail("8 packs left (8%)")
                .status("LOW")
                .build());
        alerts.add(DashboardStatsResponse.InventoryAlert.builder()
                .name("Whipped Cream")
                .detail("1 cans left (8%)")
                .status("CRITICAL")
                .build());
        return alerts;
    }

    private List<DashboardStatsResponse.AIInsight> buildAIInsights() {
        List<DashboardStatsResponse.AIInsight> insights = new ArrayList<>();
        insights.add(DashboardStatsResponse.AIInsight.builder()
                .title("Cold Brew Sales Surge")
                .impact("HIGH")
                .impactValue("+35%")
                .description("Cold brew sales increased 35% today compared to yesterday. Weather forecast shows continued warm temperatures.")
                .build());
        insights.add(DashboardStatsResponse.AIInsight.builder()
                .title("Afternoon Cappuccino Promotion")
                .impact("MEDIUM")
                .impactValue("-40%")
                .description("Cappuccino orders drop 40% between 4-6 PM. Consider promoting with a discount during these hours.")
                .build());
        insights.add(DashboardStatsResponse.AIInsight.builder()
                .title("Staff Efficiency Opportunity")
                .impact("MEDIUM")
                .impactValue("4.5 min")
                .description("Average order preparation time increased to 4.5 minutes during lunch rush. Optimal time is 3 minutes.")
                .build());
        insights.add(DashboardStatsResponse.AIInsight.builder()
                .title("Weekend Customer Pattern")
                .impact("LOW")
                .impactValue("45/hr")
                .description("Saturday morning traffic peaked at 11 AM with 45 orders/hour. Plan staffing accordingly.")
                .build());
        return insights;
    }
}