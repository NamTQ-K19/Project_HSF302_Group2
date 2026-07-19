package hsf302.se2033jv.project_hsf302_group2.barista.service.impl;

import hsf302.se2033jv.project_hsf302_group2.barista.dto.BaristaOrderDTO;
import hsf302.se2033jv.project_hsf302_group2.barista.service.interfaces.BaristaService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderItemStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderDetailRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaristaServiceImpl implements BaristaService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BaristaOrderDTO> getDashboardOrders() {
        List<Order> orders = orderRepository.findBaristaOrders();
        
        return orders.stream()
                .map(BaristaOrderDTO::new)
                .filter(dto -> dto.getItems().stream().anyMatch(item -> 
                    "PENDING".equals(item.getItemStatus()) || "PREPARING".equals(item.getItemStatus())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateItemStatus(Integer itemId, String status, String cancelReason) {
        OrderDetail item = orderDetailRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món với ID: " + itemId));

        OrderItemStatus newStatus = OrderItemStatus.valueOf(status); // ném IllegalArgumentException nếu sai, không còn bị nuốt

        if (newStatus == OrderItemStatus.CANCELLED) {
            if (cancelReason == null || cancelReason.isBlank()) {
                throw new IllegalArgumentException("Lý do hủy món không được để trống.");
            }
            if (cancelReason.length() > 500) {
                throw new IllegalArgumentException("Lý do hủy món không được vượt quá 500 ký tự.");
            }
            item.setCancelReason(cancelReason);
        }

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
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetail> getBaristaStats() {
        return orderDetailRepository.findBaristaStats();
    }
}
