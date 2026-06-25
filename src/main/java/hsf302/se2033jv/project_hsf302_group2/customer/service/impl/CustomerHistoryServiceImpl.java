// customer/service/impl/CustomerHistoryServiceImpl.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Payment;
import hsf302.se2033jv.project_hsf302_group2.common.entity.CustomerAddress;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.repository.*;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerHistoryServiceImpl implements CustomerHistoryService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    private static final String DEFAULT_IMAGE = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='60' viewBox='0 0 60 60'%3E%3Crect width='60' height='60' fill='%23f8f9fa'/%3E%3Ctext x='50%25' y='55%25' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='30' fill='%23dee2e6'%3E☕%3C/text%3E%3C/svg%3E";

    @Override
    public Page<OrderResponse> getOrderHistory(Integer userId, OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Getting order history for user: {}, status: {}, startDate: {}, endDate: {}", userId, status, startDate, endDate);

        List<Order> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        if (status != null) {
            orders = orders.stream()
                    .filter(o -> o.getOrderStatus() == status)
                    .collect(Collectors.toList());
        }

        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            orders = orders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(start) && o.getCreatedAt().isBefore(end))
                    .collect(Collectors.toList());
        }

        List<OrderResponse> responses = orders.stream()
                .map(order -> {
                    List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(order.getOrderId());
                    Payment payment = paymentRepository.findByOrder_OrderId(order.getOrderId()).orElse(null);
                    return buildOrderResponse(order, details, payment);
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<OrderResponse> pageContent = responses.isEmpty() ? List.of() : responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    public OrderResponse getOrderDetail(Integer orderId, Integer userId) {
        log.info("Getting order detail: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Bỏ qua kiểm tra quyền
        // if (!order.getUser().getUserId().equals(userId)) {
        //     throw new BusinessException("You are not authorized to view this order");
        // }

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        Payment payment = paymentRepository.findByOrder_OrderId(orderId).orElse(null);

        return buildOrderResponse(order, details, payment);
    }

    // ==================== PRIVATE METHODS ====================

    private OrderResponse buildOrderResponse(Order order, List<OrderDetail> details, Payment payment) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNumber("ORD-" + order.getOrderId());
        response.setOrderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : "PENDING");
        response.setOrderType(order.getOrderType() != null ? order.getOrderType().name() : "ONLINE");
        response.setSubtotal(order.getSubtotal());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setShippingFee(order.getShippingFee());
        response.setTotalAmount(order.getTotalAmount());
        response.setPointsEarned(order.getPointsEarned());
        response.setNote(order.getNote());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Kiểm tra đã review hết chưa
        boolean fullyReviewed = false;
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            boolean allReviewed = true;
            Integer userId = order.getUser().getUserId();
            for (OrderDetail detail : details) {
                boolean reviewed = reviewRepository.existsByCustomer_UserIdAndOrder_OrderIdAndProduct_ProductId(
                        userId, order.getOrderId(), detail.getProduct().getProductId());
                if (!reviewed) {
                    allReviewed = false;
                    break;
                }
            }
            fullyReviewed = allReviewed && !details.isEmpty();
        }
        response.setFullyReviewed(fullyReviewed);

        // Order items
        List<OrderResponse.OrderItemResponse> items = details.stream()
                .map(detail -> {
                    OrderResponse.OrderItemResponse item = new OrderResponse.OrderItemResponse();
                    item.setItemId(detail.getItemId());
                    item.setProductName(detail.getProductNameSnapshot());
                    item.setVariantName(detail.getVariantNameSnapshot());
                    item.setQuantity(detail.getQuantity());
                    item.setPrice(detail.getPriceSnapshot());
                    item.setItemTotal(detail.getItemTotal());
                    item.setSpecialNote(detail.getSpecialNote());
                    item.setItemStatus(detail.getItemStatus() != null ? detail.getItemStatus().name() : "PENDING");

                    // Kiểm tra sản phẩm đã được đánh giá chưa
                    boolean reviewed = reviewRepository.existsByCustomer_UserIdAndOrder_OrderIdAndProduct_ProductId(
                            order.getUser().getUserId(), order.getOrderId(), detail.getProduct().getProductId());
                    item.setReviewed(reviewed);

                    String imageUrl = DEFAULT_IMAGE;
                    if (detail.getProduct() != null && detail.getProduct().getImages() != null && !detail.getProduct().getImages().isEmpty()) {
                        if (detail.getProduct().getImages().get(0) != null) {
                            imageUrl = detail.getProduct().getImages().get(0).getImageUrl();
                        }
                    }
                    item.setProductImage(imageUrl);

                    return item;
                })
                .collect(Collectors.toList());
        response.setItems(items);

        // Delivery address
        if (order.getDeliveryAddress() != null) {
            CustomerAddress addr = order.getDeliveryAddress();
            OrderResponse.DeliveryAddressResponse address = new OrderResponse.DeliveryAddressResponse();
            address.setAddressId(addr.getAddressId());
            address.setLabel(addr.getLabel());
            address.setFullAddress(addr.getFullAddress());
            address.setRecipientName(addr.getRecipientName());
            address.setRecipientPhone(addr.getRecipientPhone());
            response.setDeliveryAddress(address);
        }

        // Payment
        if (payment != null) {
            OrderResponse.PaymentResponse paymentResponse = new OrderResponse.PaymentResponse();
            paymentResponse.setPaymentId(payment.getPaymentId());
            paymentResponse.setPaymentMethod(payment.getPaymentMethod().getName());
            paymentResponse.setAmount(payment.getAmount());
            paymentResponse.setPaymentStatus(payment.getPaymentStatus().name());
            paymentResponse.setTransactionRef(payment.getTransactionRef());
            paymentResponse.setPaidAt(payment.getPaidAt());
            response.setPayment(paymentResponse);
        }

        return response;
    }
}