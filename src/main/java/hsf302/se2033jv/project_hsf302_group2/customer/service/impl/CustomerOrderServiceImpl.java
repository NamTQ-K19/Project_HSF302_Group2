// customer/service/impl/CustomerOrderServiceImpl.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.*;
import hsf302.se2033jv.project_hsf302_group2.common.enums.*;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.*;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.PlaceOrderRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderConfirmationResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerOrderService;
import hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final LoyaltyPointRepository loyaltyPointRepository;
    private final ReviewRepository reviewRepository;
    private final InvoiceService invoiceService;
    private static final String CASH_PAYMENT_METHOD = "Tiền mặt";

    private static final Integer CUSTOMER_ROLE_ID = 5;
    private static final String DEFAULT_IMAGE = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='60' viewBox='0 0 60 60'%3E%3Crect width='60' height='60' fill='%23f8f9fa'/%3E%3Ctext x='50%25' y='55%25' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='30' fill='%23dee2e6'%3E☕%3C/text%3E%3C/svg%3E";

    @Override
    @Transactional
    public OrderConfirmationResponse placeOnlineOrder(Integer userId, PlaceOrderRequest request) {
        log.info("Placing online order for user: {}", userId);

        // 1. Validate user
        User user = validateCustomer(userId);

        // 2. Validate delivery address
        CustomerAddress address = customerAddressRepository
                .findByAddressIdAndCustomer_UserId(request.getDeliveryAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found"));

        // 3. Validate payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        // 4. Get cart
        Cart cart = cartRepository.findByCustomer_UserId(userId)
                .orElseThrow(() -> new BusinessException("Cart not found"));

        // 5. Lấy cart items - Nếu có itemIds thì chỉ lấy những item đã chọn
        List<CartItem> cartItems;
        List<Integer> selectedItemIds = request.getItemIds();
        List<Integer> itemsToRemove = new ArrayList<>();

        if (selectedItemIds != null && !selectedItemIds.isEmpty()) {
            cartItems = cartItemRepository.findAllById(selectedItemIds);

            for (CartItem item : cartItems) {
                if (!item.getCart().getCustomer().getUserId().equals(userId)) {
                    throw new BusinessException("You are not authorized to access this item");
                }
            }

            if (cartItems.isEmpty()) {
                throw new BusinessException("No items selected for checkout");
            }
            log.info("Selected {} items for checkout", cartItems.size());

            itemsToRemove = new ArrayList<>(selectedItemIds);
        } else {
            cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
            if (cartItems.isEmpty()) {
                throw new BusinessException("Cart is empty");
            }
            log.info("All {} items in cart will be checked out", cartItems.size());

            for (CartItem item : cartItems) {
                itemsToRemove.add(item.getCartItemId());
            }
        }

        // 6. Validate and create order details
        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getVariant();
            if (variant == null || !variant.getIsAvailable()) {
                throw new BusinessException("Product variant not available: " +
                        (variant != null ? variant.getVariantName() : "Unknown"));
            }

            Product product = cartItem.getProduct();
            if (product == null || !product.getIsActive()) {
                throw new BusinessException("Product is not active: " +
                        (product != null ? product.getName() : "Unknown"));
            }

            BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            OrderDetail detail = OrderDetail.builder()
                    .product(product)
                    .variant(variant)
                    .productNameSnapshot(product.getName())
                    .variantNameSnapshot(variant.getVariantName())
                    .priceSnapshot(variant.getPrice())
                    .quantity(cartItem.getQuantity())
                    .itemTotal(itemTotal)
                    .specialNote(cartItem.getSpecialNote())
                    .itemStatus(OrderItemStatus.PENDING)
                    .build();

            orderDetails.add(detail);
        }

        // 7. Xử lý giảm giá bằng điểm
        BigDecimal discountAmount = BigDecimal.ZERO;
        Integer pointsUsed = 0;

        if (request.getUsePoints() != null && request.getUsePoints()) {
            Integer currentPoints = getCurrentPoints(userId);
            if (currentPoints > 0) {
                BigDecimal maxDiscount = subtotal.multiply(new BigDecimal("0.5"));
                int maxPointsCanUse = maxDiscount.divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue();
                int pointsToUse = Math.min(currentPoints, maxPointsCanUse);

                if (pointsToUse > 0) {
                    discountAmount = new BigDecimal(pointsToUse * 100);
                    pointsUsed = pointsToUse;
                    // ĐÃ CHUYỂN việc tạo LoyaltyPoint REDEEM xuống bước 8 (sau khi có savedOrder.getOrderId())
                    // để tránh reference_id bị NULL
                }
            }
        }

        BigDecimal finalAmount = subtotal.subtract(discountAmount);
        Integer pointsEarned = calculatePointsEarned(finalAmount);

        // 8. Create order
        Order order = Order.builder()
                .user(user)
                .orderType(OrderType.ONLINE)
                .orderStatus(OrderStatus.PENDING)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(finalAmount)
                .pointsEarned(pointsEarned)
                .note(request.getNote())
                .deliveryAddress(address)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getOrderId());

        // ← THÊM: Tạo LoyaltyPoint REDEEM tại đây — LÚC NÀY đã có savedOrder.getOrderId()
        if (pointsUsed > 0) {
            LoyaltyPoint redeemPoint = LoyaltyPoint.builder()
                    .customer(user)
                    .transactionType(TransactionType.REDEEM)
                    .points(-pointsUsed)
                    .balanceAfter(getCurrentPoints(userId) - pointsUsed)
                    .referenceType(ReferenceType.ORDER)
                    .referenceId(savedOrder.getOrderId())   // ← FIX chính: gắn đúng order_id
                    .note("Đổi điểm giảm giá đơn hàng #" + savedOrder.getOrderId())
                    .createdAt(LocalDateTime.now())
                    .build();
            loyaltyPointRepository.save(redeemPoint);
        }

        // 9. Save order details
        for (OrderDetail detail : orderDetails) {
            detail.setOrder(savedOrder);
            orderDetailRepository.save(detail);
        }

        // 10. Chỉ xóa những item đã được thanh toán
        log.info("Removing {} items from cart: {}", itemsToRemove.size(), itemsToRemove);
        for (Integer itemId : itemsToRemove) {
            cartItemRepository.deleteById(itemId);
        }
        log.info("Removed {} items from cart", itemsToRemove.size());

        // 11. Create payment — LUÔN PENDING khi khởi tạo, kể cả Tiền mặt
        String transactionRef = "PAY-" + savedOrder.getOrderId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        Payment payment = createPayment(savedOrder, paymentMethod, finalAmount, transactionRef);

        // 12. KHÔNG tự động CONFIRMED, KHÔNG cộng điểm ở bước này.
        // - Order.status luôn giữ PENDING; chỉ Cashier xác nhận mới chuyển CONFIRMED (use case khác, chưa implement).
        // - Payment (Tiền mặt): giữ PENDING, chờ Cashier xác nhận thành SUCCESS/FAILED (use case khác).
        // - Payment (Online/VNPay): sẽ được cập nhật SUCCESS/FAILED ngay khi có callback từ Gateway
        //   (xử lý ở handleGatewayPaymentResult), nhưng KHÔNG kéo theo đổi Order.status.
        // - Điểm tích lũy CHỈ cộng khi Order.status = COMPLETED (use case Cashier/Manager hoàn tất đơn — chưa implement).

        Integer totalPoints = getCurrentPoints(userId); // điểm hiện có, chưa cộng gì thêm ở bước này

        // 13. Build response
        return OrderConfirmationResponse.builder()
                .orderId(savedOrder.getOrderId())
                .orderNumber("ORD-" + savedOrder.getOrderId())
                .totalAmount(finalAmount)
                .paymentStatus(payment.getPaymentStatus().name())
                .transactionRef(transactionRef)
                .pointsEarned(pointsEarned)   // chỉ hiển thị số điểm SẼ nhận, chưa thực ghi vào loyalty_points
                .totalPoints(totalPoints)
                .estimatedTime(LocalDateTime.now().plusMinutes(15))
                .message("Đặt hàng thành công! Đơn hàng của bạn đang chờ xác nhận từ nhân viên.")
                .paymentMethodName(paymentMethod.getName())
                .build();
    }

    @Override
    public OrderResponse getOrderDetails(Integer orderId, Integer userId) {
        log.info("Getting order details: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException("You are not authorized to view this order");
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        Payment payment = paymentRepository.findByOrder_OrderId(orderId).orElse(null);
        return buildOrderResponse(order, details, payment);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Integer orderId, Integer userId, String reason) {
        log.info("Cancelling order: orderId={}, userId={}, reason={}", orderId, userId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException("You are not authorized to cancel this order");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING &&
                order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Order cannot be cancelled in current status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        Order cancelledOrder = orderRepository.save(order);

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        for (OrderDetail detail : details) {
            detail.setItemStatus(OrderItemStatus.CANCELLED);
            orderDetailRepository.save(detail);
        }

        Payment payment = paymentRepository.findByOrder_OrderId(orderId).orElse(null);
        if (payment != null && payment.getPaymentStatus() == PaymentStatus.PENDING) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }

        // ← THÊM: Hoàn điểm đã dùng để giảm giá (nếu có), chỉ khi Payment CHƯA từng thành công.
        // Nếu Payment đã SUCCESS (khách bấm Hủy sau khi VNPay đã báo thành công), đây là tình huống
        // đã phát sinh giao dịch tiền thật — cần luồng hoàn tiền/hoàn điểm thủ công riêng, KHÔNG tự động ở đây.
        if (payment == null || payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            refundUsedPointsIfAny(order);
        } else {
            log.warn("Order {} cancelled while payment already SUCCESS — points NOT auto-refunded, requires manual handling", orderId);
        }

        log.info("Order {} cancelled successfully with {} items", orderId, details.size());

        return buildOrderResponse(cancelledOrder, details, payment);
    }

    // ==================== PRIVATE METHODS ====================

    private User validateCustomer(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == null || user.getRole().getRoleId() != CUSTOMER_ROLE_ID) {
            throw new BusinessException("Only customers can place online orders");
        }

        if (!user.getStatus()) {
            throw new BusinessException("User account is locked");
        }

        return user;
    }

    private Integer calculatePointsEarned(BigDecimal amount) {
        return amount.divide(new BigDecimal("10000"), 0, RoundingMode.FLOOR).intValue();
    }

    private Integer getCurrentPoints(Integer customerId) {
        Integer points = loyaltyPointRepository.getTotalPointsByCustomerId(customerId);
        return points != null ? points : 0;
    }

    private Payment createPayment(Order order, PaymentMethod paymentMethod, BigDecimal amount, String transactionRef) {
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .paymentStatus(PaymentStatus.PENDING)
                .transactionRef(transactionRef)
                .createdAt(LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);
    }

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

    @Override
    @Transactional
    public void handleGatewayPaymentResult(Integer orderId, boolean success, String transactionNo, String rawResponse) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        // Chặn xử lý trùng nếu Payment đã có kết quả cuối cùng là SUCCESS
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            log.info("Order {} already processed as SUCCESS, skip", orderId);
            return;
        }

        payment.setGatewayResponse(rawResponse);

        if (success) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            if (transactionNo != null) payment.setTransactionRef(transactionNo);
            paymentRepository.save(payment);

            // Tự động gửi email hóa đơn ngay sau khi thanh toán online thành công
            try {
                invoiceService.resendInvoiceEmail(orderId);
            } catch (Exception e) {
                log.error("Không thể tự động gửi email hóa đơn cho đơn #{}: {}", orderId, e.getMessage(), e);
            }

            // KHÔNG set order.status = CONFIRMED — chờ Cashier xác nhận (use case khác)
            // KHÔNG cộng điểm — chỉ cộng khi Order.status = COMPLETED (use case khác)
            log.info("Order {} payment SUCCESS via gateway, waiting for Cashier confirmation", orderId);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            // Order vẫn giữ PENDING

            refundUsedPointsIfAny(order); // ← mục 6: hoàn điểm đã dùng để giảm giá (nếu có)
            log.info("Order {} payment FAILED via gateway, refunded used points if any", orderId);
        }
    }

    /**
     * Cộng điểm tích lũy cho đơn hàng.
     * ⚠️ CHƯA được gọi ở bất kỳ đâu trong use case Payment này.
     * Sẽ được gọi từ use case "Cashier/Manager xác nhận hoàn tất đơn hàng"
     * khi Order.status chuyển sang COMPLETED — use case đó chưa implement.
     */
    private void creditEarnedPoints(Order order) {
        Integer pointsEarned = order.getPointsEarned();
        if (pointsEarned == null || pointsEarned <= 0) return;

        boolean alreadyCredited = loyaltyPointRepository.existsByReferenceTypeAndReferenceIdAndTransactionType(
                ReferenceType.ORDER, order.getOrderId(), TransactionType.EARN);
        if (alreadyCredited) return;

        User customer = order.getUser();
        Integer newBalance = getCurrentPoints(customer.getUserId()) + pointsEarned;

        LoyaltyPoint earn = LoyaltyPoint.builder()
                .customer(customer)
                .transactionType(TransactionType.EARN)
                .points(pointsEarned)
                .balanceAfter(newBalance)
                .referenceType(ReferenceType.ORDER)
                .referenceId(order.getOrderId())
                .note("Tích điểm từ đơn hàng #" + order.getOrderId())
                .createdAt(LocalDateTime.now())
                .build();
        loyaltyPointRepository.save(earn);
    }

    private void refundUsedPointsIfAny(Order order) {
        if (order.getDiscountAmount() == null || order.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) return;

        // ← THÊM: Chặn hoàn điểm 2 lần cho cùng 1 order — trường hợp VNPay báo fail
        // (handleGatewayPaymentResult) RỒI khách còn bấm "Hủy đơn hàng" thủ công sau đó (cancelOrder)
        boolean alreadyRefunded = loyaltyPointRepository.existsByReferenceTypeAndReferenceIdAndTransactionType(
                ReferenceType.ORDER, order.getOrderId(), TransactionType.ADJUST);
        if (alreadyRefunded) {
            log.info("Order {} points already refunded before, skip duplicate refund", order.getOrderId());
            return;
        }

        int pointsToRefund = order.getDiscountAmount().divide(new BigDecimal("100")).intValue();
        User customer = order.getUser();
        Integer newBalance = getCurrentPoints(customer.getUserId()) + pointsToRefund;

        LoyaltyPoint refund = LoyaltyPoint.builder()
                .customer(customer)
                .transactionType(TransactionType.ADJUST)
                .points(pointsToRefund)
                .balanceAfter(newBalance)
                .referenceType(ReferenceType.ORDER)
                .referenceId(order.getOrderId())
                .note("Hoàn điểm do thanh toán đơn hàng #" + order.getOrderId() + " không thành công")
                .createdAt(LocalDateTime.now())
                .build();
        loyaltyPointRepository.save(refund);
    }

}