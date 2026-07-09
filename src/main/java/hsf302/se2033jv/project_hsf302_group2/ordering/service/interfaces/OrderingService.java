package hsf302.se2033jv.project_hsf302_group2.ordering.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.ConfirmPaymentRequest;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.CreateOrderRequest;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.OrderFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.UpdateStatusRequest;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.response.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderingService {
    List<ProductPosResponse> getPosProducts(Integer categoryId, String keyword);
    List<TablePosResponse> getPosTables();
    List<CustomerPosResponse> searchCustomers(String keyword);
    OrderPosDetailResponse createCounterOrder(CreateOrderRequest request, String cashierUsername);
    Page<OrderPosListResponse> getOrders(OrderFilterRequest filter, int page, int size);
    OrderPosDetailResponse getOrderDetail(Integer orderId);
    OrderPosDetailResponse updateOrderStatus(Integer orderId, UpdateStatusRequest request);
    OrderPosDetailResponse confirmPayment(Integer orderId, ConfirmPaymentRequest request);
}
