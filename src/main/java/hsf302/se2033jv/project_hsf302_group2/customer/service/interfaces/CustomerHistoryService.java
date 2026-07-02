// customer/service/interfaces/CustomerHistoryService.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface CustomerHistoryService {

    /**
     * Lấy lịch sử đơn hàng với bộ lọc
     */
    Page<OrderResponse> getOrderHistory(Integer userId, OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Lấy chi tiết đơn hàng
     */
    OrderResponse getOrderDetail(Integer orderId, Integer userId);
}