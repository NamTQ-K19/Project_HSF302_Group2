// customer/controller/CustomerHistoryController.java
package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.util.SecurityUtils;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Slf4j
@Controller
@RequestMapping("/customer/orders")
@RequiredArgsConstructor
public class CustomerHistoryController {

    private final CustomerHistoryService historyService;

    /**
     * Hiển thị lịch sử đơn hàng
     */
    @GetMapping("/history")
    public String getOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        log.info("Getting order history for user: {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        LocalDate fromDate = startDate != null && !startDate.isEmpty() ? LocalDate.parse(startDate) : null;
        LocalDate toDate = endDate != null && !endDate.isEmpty() ? LocalDate.parse(endDate) : null;

        OrderStatus orderStatus = status != null && !status.isEmpty() ? OrderStatus.valueOf(status) : null;

        Page<OrderResponse> orders = historyService.getOrderHistory(userId, orderStatus, fromDate, toDate, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageTitle", "Lịch sử đơn hàng");

        return "customer/order/history";
    }

    /**
     * Hiển thị chi tiết đơn hàng
     */
    @GetMapping("/detail/{orderId}")
    public String getOrderDetail(@PathVariable Integer orderId, Model model) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        log.info("Getting order detail for user: {}, orderId: {}", userId, orderId);

        OrderResponse order = historyService.getOrderDetail(orderId, userId);
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Chi tiết đơn hàng");

        return "customer/order/detail";
    }
}