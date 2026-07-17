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
        OrderDetail item = orderDetailRepository.findById(itemId).orElse(null);
        if (item != null) {
            try {
                OrderItemStatus newStatus = OrderItemStatus.valueOf(status);
                item.setItemStatus(newStatus);
                if (newStatus == OrderItemStatus.CANCELLED && cancelReason != null) {
                    item.setCancelReason(cancelReason);
                }
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
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetail> getBaristaStats() {
        return orderDetailRepository.findBaristaStats();
    }
}
