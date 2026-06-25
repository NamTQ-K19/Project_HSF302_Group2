// customer/service/interfaces/CustomerOrderService.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.PlaceOrderRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderConfirmationResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderResponse;

public interface CustomerOrderService {

    OrderConfirmationResponse placeOnlineOrder(Long userId, PlaceOrderRequest request);
    OrderResponse cancelOrder(Integer orderId, Long userId, String reason);
    OrderResponse getOrderDetails(Integer orderId, Long userId);
}