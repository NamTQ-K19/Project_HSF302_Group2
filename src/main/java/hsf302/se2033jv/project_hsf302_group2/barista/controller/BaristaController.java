package hsf302.se2033jv.project_hsf302_group2.barista.controller;

import hsf302.se2033jv.project_hsf302_group2.barista.dto.BaristaOrderDTO;
import hsf302.se2033jv.project_hsf302_group2.barista.service.interfaces.BaristaService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderItemStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BaristaController {

    @Autowired
    private BaristaService baristaService;

    @GetMapping("/barista/dashboard")
    public String baristaDashboard(Model model) {
        List<BaristaOrderDTO> dtos = baristaService.getDashboardOrders();
                
        model.addAttribute("orders", dtos);
        return "barista/barista-dashboard";
    }

    @PostMapping("/barista/item/status")
    public String updateItemStatus(
            @RequestParam("itemId") Integer itemId, 
            @RequestParam("status") String status,
            @RequestParam(required = false) String cancelReason) {
        
        baristaService.updateItemStatus(itemId, status, cancelReason);
        
        return "redirect:/barista/dashboard";
    }

    @GetMapping("/barista/statistics")
    public String statistics(Model model) {
        List<OrderDetail> stats = baristaService.getBaristaStats();
        
        List<OrderDetail> completedItems = stats.stream()
                .filter(item -> item.getItemStatus() == OrderItemStatus.COMPLETED)
                .collect(Collectors.toList());
                
        List<OrderDetail> cancelledItems = stats.stream()
                .filter(item -> item.getItemStatus() == OrderItemStatus.CANCELLED)
                .collect(Collectors.toList());
                
        // Top 5 sản phẩm pha thành công nhiều nhất
        java.util.Map<String, Integer> productQuantities = completedItems.stream()
                .collect(Collectors.groupingBy(
                        OrderDetail::getProductNameSnapshot,
                        Collectors.summingInt(OrderDetail::getQuantity)
                ));

        List<java.util.Map.Entry<String, Integer>> topProducts = productQuantities.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        int maxTopQuantity = topProducts.stream().mapToInt(java.util.Map.Entry::getValue).max().orElse(1);

        // Thống kê lý do hủy
        java.util.Map<String, List<OrderDetail>> groupedCancel = cancelledItems.stream()
                .collect(Collectors.groupingBy(item -> (item.getCancelReason() != null && !item.getCancelReason().trim().isEmpty()) ? item.getCancelReason() : "Khác"));

        int totalCancelQuantity = cancelledItems.stream().mapToInt(OrderDetail::getQuantity).sum();

        List<java.util.Map<String, Object>> cancelReasonStats = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, List<OrderDetail>> entry : groupedCancel.entrySet()) {
            int count = entry.getValue().size(); // Số lần
            int qty = entry.getValue().stream().mapToInt(OrderDetail::getQuantity).sum(); // Số ly
            int percentage = totalCancelQuantity > 0 ? (int) Math.round((qty * 100.0) / totalCancelQuantity) : 0;
            
            java.util.Map<String, Object> stat = new java.util.HashMap<>();
            stat.put("reason", entry.getKey());
            stat.put("count", count);
            stat.put("quantity", qty);
            stat.put("percentage", percentage);
            cancelReasonStats.add(stat);
        }
        
        cancelReasonStats.sort((s1, s2) -> Integer.compare((int)s2.get("quantity"), (int)s1.get("quantity")));

        stats.sort((s1, s2) -> s2.getOrder().getUpdatedAt().compareTo(s1.getOrder().getUpdatedAt()));

        int totalItemsQty = stats.stream().mapToInt(OrderDetail::getQuantity).sum();
        int completedQty = completedItems.stream().mapToInt(OrderDetail::getQuantity).sum();
        int completionRate = stats.isEmpty() ? 0 : (int) Math.round((completedItems.size() * 100.0) / stats.size());

        model.addAttribute("totalItemsCount", stats.size());
        model.addAttribute("totalItemsQty", totalItemsQty);
        model.addAttribute("completedQty", completedQty);
        model.addAttribute("completionRate", completionRate);
        model.addAttribute("cancelledQty", totalCancelQuantity);

        model.addAttribute("allStats", stats);
        model.addAttribute("completedItems", completedItems);
        model.addAttribute("cancelledItems", cancelledItems);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("maxTopQuantity", maxTopQuantity);
        model.addAttribute("cancelReasonStats", cancelReasonStats);
        
        return "barista/statistics";
    }
}
