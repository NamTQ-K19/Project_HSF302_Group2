package hsf302.se2033jv.project_hsf302_group2.barista.controller;

import hsf302.se2033jv.project_hsf302_group2.barista.dto.BaristaOrderDTO;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderItemStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderDetailRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping("/barista/dashboard")
    @Transactional(readOnly = true)
    public String baristaDashboard(Model model) {
        List<Order> orders = orderRepository.findBaristaOrders();
        
        List<BaristaOrderDTO> dtos = orders.stream()
                .map(BaristaOrderDTO::new)
                .filter(dto -> dto.getItems().stream().anyMatch(item -> 
                    "PENDING".equals(item.getItemStatus()) || "PREPARING".equals(item.getItemStatus())
                ))
                .collect(Collectors.toList());
                
        model.addAttribute("orders", dtos);
        return "barista/barista-dashboard";
    }

    @PostMapping("/barista/item/status")
    public String updateItemStatus(@RequestParam("itemId") Integer itemId, @RequestParam("status") String status) {
        OrderDetail item = orderDetailRepository.findById(itemId).orElse(null);
        if (item != null) {
            try {
                OrderItemStatus newStatus = OrderItemStatus.valueOf(status);
                item.setItemStatus(newStatus);
                orderDetailRepository.save(item);

                Order order = item.getOrder();
                if (newStatus == OrderItemStatus.COMPLETED) {
                    boolean allCompleted = true;
                    for (OrderDetail d : order.getOrderDetails()) {
                        if (d.getItemStatus() != OrderItemStatus.COMPLETED && d.getItemStatus() != OrderItemStatus.CANCELLED) {
                            allCompleted = false;
                            break;
                        }
                    }
                    if (allCompleted) {
                        order.setOrderStatus(OrderStatus.READY);
                        orderRepository.save(order);
                    }
                } else if (newStatus == OrderItemStatus.PREPARING) {
                    if (order.getOrderStatus() == OrderStatus.PENDING || order.getOrderStatus() == OrderStatus.CONFIRMED) {
                        order.setOrderStatus(OrderStatus.PREPARING);
                        orderRepository.save(order);
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return "redirect:/barista/dashboard";
    }
}
