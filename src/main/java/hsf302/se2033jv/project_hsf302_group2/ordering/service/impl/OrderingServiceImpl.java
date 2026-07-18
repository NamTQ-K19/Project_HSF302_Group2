package hsf302.se2033jv.project_hsf302_group2.ordering.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.*;
import hsf302.se2033jv.project_hsf302_group2.common.enums.*;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.*;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.*;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.response.*;
import hsf302.se2033jv.project_hsf302_group2.ordering.service.interfaces.OrderingService;
import hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class OrderingServiceImpl implements OrderingService {

    private final ProductRepository productRepository;
    private final CoffeeTableRepository coffeeTableRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final LoyaltyPointRepository loyaltyPointRepository;
    private final PolicyRepository policyRepository;
    private final InvoiceService invoiceService;

    private static final String DEFAULT_IMAGE = "/images/default-product.png";

    @Override
    @Transactional(readOnly = true)
    public List<ProductPosResponse> getPosProducts(Integer categoryId, String keyword) {
        List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.searchByKeyword(keyword.trim());
        } else if (categoryId != null && categoryId > 0) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAllActiveAvailable();
        }

        return products.stream().map(product -> {
            String imgUrl = DEFAULT_IMAGE;
            if (product.getImages() != null && !product.getImages().isEmpty() && product.getImages().get(0) != null) {
                imgUrl = product.getImages().get(0).getImageUrl();
            }

            List<ProductPosResponse.VariantPosResponse> variantResponses = new ArrayList<>();
            if (product.getVariants() != null) {
                variantResponses = product.getVariants().stream()
                        .filter(v -> v.getIsAvailable() != null && v.getIsAvailable())
                        .map(v -> ProductPosResponse.VariantPosResponse.builder()
                                .variantId(v.getVariantId())
                                .variantName(v.getVariantName())
                                .size(v.getSize() != null ? v.getSize().name() : "")
                                .temperature(v.getTemperature() != null ? v.getTemperature().name() : "")
                                .price(v.getPrice() != null ? v.getPrice() : BigDecimal.ZERO)
                                .isAvailable(v.getIsAvailable())
                                .build())
                        .collect(Collectors.toList());
            }

            return ProductPosResponse.builder()
                    .productId(product.getProductId())
                    .name(product.getName())
                    .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                    .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                    .imageUrl(imgUrl)
                    .description(product.getDescription())
                    .variants(variantResponses)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TablePosResponse> getPosTables() {
        // Suy ra bàn nào đang OCCUPIED dựa trên Order đang mở, không lưu status trực tiếp trên bàn
        List<Integer> occupiedTableIds = orderRepository.findActiveOrderTableIds();

        return coffeeTableRepository.findByIsActiveTrueOrderByTableIdAsc().stream()
                .map(t -> TablePosResponse.builder()
                        .tableId(t.getTableId())
                        .capacity(t.getCapacity())
                        .status(occupiedTableIds.contains(t.getTableId())
                                ? TableStatus.OCCUPIED.name()
                                : TableStatus.AVAILABLE.name())
                        .isActive(t.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerPosResponse> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Pageable limit = PageRequest.of(0, 10);
        Page<User> users = userRepository.searchByKeyword(keyword.trim(), limit);
        return users.getContent().stream()
                .map(u -> CustomerPosResponse.builder()
                        .userId(u.getUserId())
                        .fullName((u.getFirstName() != null ? u.getFirstName() : "") + " " + (u.getLastName() != null ? u.getLastName() : ""))
                        .phone(u.getPhone())
                        .email(u.getEmail())
                        .loyaltyPoints(getCustomerPoints(u.getUserId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderPosDetailResponse createCounterOrder(CreateOrderRequest request, String cashierUsername) {
        log.info("Creating counter order by cashier: {}, request: {}", cashierUsername, request);
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Đơn hàng phải có ít nhất 1 sản phẩm");
        }

        // 1. Determine user/customer
        User customer = null;
        if (request.getCustomerId() != null && request.getCustomerId() > 0) {
            customer = userRepository.findById(request.getCustomerId()).orElse(null);
        }
        if (customer == null) {
            customer = userRepository.findByUsername(cashierUsername)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản người dùng"));
        }

        // 2. Determine table
        CoffeeTable table = null;
        if (request.getTableId() != null && request.getTableId() > 0) {
            table = coffeeTableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn #" + request.getTableId()));
            // Không cần set status thủ công — bàn tự động coi là OCCUPIED
            // khi Order này được lưu với table gắn vào (do getPosTables() suy ra từ Order)
        }

        // 3. Calculate order details
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm #" + itemReq.getProductId()));
            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getVariantId().equals(itemReq.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy variant #" + itemReq.getVariantId()));

            int qty = (itemReq.getQuantity() != null && itemReq.getQuantity() > 0) ? itemReq.getQuantity() : 1;
            BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(qty));
            subtotal = subtotal.add(itemTotal);

            OrderDetail detail = OrderDetail.builder()
                    .product(product)
                    .variant(variant)
                    .productNameSnapshot(product.getName())
                    .variantNameSnapshot(variant.getVariantName())
                    .priceSnapshot(variant.getPrice())
                    .quantity(qty)
                    .itemTotal(itemTotal)
                    .specialNote(itemReq.getSpecialNote())
                    .itemStatus(OrderItemStatus.PENDING)
                    .build();
            orderDetails.add(detail);
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        int pointsUsed = 0;
        if (customer != null && (Boolean.TRUE.equals(request.getUsePoints()) || (request.getPointsToUse() != null && request.getPointsToUse() > 0))) {
            BigDecimal pointToVndRate = getPointToVndRate();   // đọc động từ policies

            if (pointToVndRate.compareTo(BigDecimal.ZERO) > 0) {
                int currentPoints = getCustomerPoints(customer.getUserId());
                if (currentPoints > 0) {
                    BigDecimal maxDiscount = subtotal.multiply(new BigDecimal("0.5"));
                    int maxPointsCanUse = maxDiscount.divide(pointToVndRate, 0, RoundingMode.FLOOR).intValue();
                    int pointsToUse = Math.min(currentPoints, maxPointsCanUse);
                    if (request.getPointsToUse() != null && request.getPointsToUse() > 0) {
                        pointsToUse = Math.min(request.getPointsToUse(), Math.min(currentPoints, maxPointsCanUse));
                    }
                    if (pointsToUse > 0) {
                        discountAmount = pointToVndRate.multiply(new BigDecimal(pointsToUse));
                        pointsUsed = pointsToUse;
                    }
                }
            }
            // Nếu chính sách "Đổi điểm" đang bị Manager vô hiệu hóa (pointToVndRate = 0),
            // Cashier vẫn tick "Sử dụng điểm" được nhưng không có gì được áp dụng.
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }
        int pointsEarned = calculatePointsEarned(subtotal);   // ← FIX: tính điểm theo giá gốc (subtotal), không trừ giảm giá

        // 4. Create Order
        Order order = Order.builder()
                .user(customer)
                .table(table)
                .orderType(request.getOrderType() != null ? request.getOrderType() : OrderType.COUNTER)
                .orderStatus(OrderStatus.CONFIRMED)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .pointsEarned(pointsEarned)
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Order savedOrder = orderRepository.save(order);

        if (pointsUsed > 0 && customer != null) {
            LoyaltyPoint redeemPoint = LoyaltyPoint.builder()
                    .customer(customer)
                    .transactionType(TransactionType.REDEEM)
                    .points(-pointsUsed)
                    .balanceAfter(getCustomerPoints(customer.getUserId()) - pointsUsed)
                    .referenceType(ReferenceType.ORDER)
                    .referenceId(savedOrder.getOrderId())
                    .note("Đổi điểm giảm giá đơn hàng POS #" + savedOrder.getOrderId())
                    .createdAt(LocalDateTime.now())
                    .build();
            loyaltyPointRepository.save(redeemPoint);
        }

        for (OrderDetail detail : orderDetails) {
            detail.setOrder(savedOrder);
            orderDetailRepository.save(detail);
        }

        // 5. Create Payment
        PaymentMethod paymentMethod = null;
        if (request.getPaymentMethodId() != null && request.getPaymentMethodId() > 0) {
            paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId()).orElse(null);
        }
        if (paymentMethod == null) {
            paymentMethod = paymentMethodRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BusinessException("Hệ thống chưa có phương thức thanh toán nào"));
        }

        PaymentStatus paymentStatus = PaymentStatus.PENDING;
        LocalDateTime paidAt = null;
        if (Boolean.TRUE.equals(request.getIsPaidImmediately())) {
            paymentStatus = PaymentStatus.SUCCESS;
            paidAt = LocalDateTime.now();
            if (pointsEarned > 0 && customer != null) {
                LoyaltyPoint lp = LoyaltyPoint.builder()
                        .customer(customer)
                        .transactionType(TransactionType.EARN)
                        .points(pointsEarned)
                        .balanceAfter(getCustomerPoints(customer.getUserId()) + pointsEarned)
                        .referenceType(ReferenceType.ORDER)
                        .referenceId(savedOrder.getOrderId())
                        .note("Tích điểm từ đơn POS #" + savedOrder.getOrderId())
                        .createdAt(LocalDateTime.now())
                        .build();
                loyaltyPointRepository.save(lp);
            }
        }

        String txRef = "POS-" + savedOrder.getOrderId() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(paymentMethod)
                .amount(totalAmount)
                .paymentStatus(paymentStatus)
                .transactionRef(txRef)
                .paidAt(paidAt)
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        return mapToDetailResponse(savedOrder, orderDetails, payment);
    }

    /**
     * Đọc tỷ lệ quy đổi "1 điểm = X VND" từ policies (REDEEM + DISCOUNT).
     * Trả về 0 nếu chính sách không tồn tại hoặc đang bị vô hiệu hóa.
     */
    private BigDecimal getPointToVndRate() {
        return policyRepository.findByPolicyTypeAndActionType(PolicyType.REDEEM, PolicyActionType.DISCOUNT)
                .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                .map(Policy::getCurrencyValue)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Tính điểm tích lũy từ tổng tiền đơn hàng, dựa trên chính sách EARN + ORDER (đơn vị %).
     * Trả về 0 nếu chính sách không tồn tại hoặc đang bị vô hiệu hóa.
     */
    private int calculatePointsEarned(BigDecimal totalAmount) {
        BigDecimal earnPercent = policyRepository.findByPolicyTypeAndActionType(PolicyType.EARN, PolicyActionType.ORDER)
                .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                .map(Policy::getCurrencyValue)
                .orElse(BigDecimal.ZERO);

        if (earnPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal rate = earnPercent.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        return totalAmount.multiply(rate).setScale(0, RoundingMode.FLOOR).intValue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderPosListResponse> getOrders(OrderFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Integer orderIdVal = null;
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            try {
                orderIdVal = Integer.parseInt(filter.getKeyword().trim());
            } catch (NumberFormatException ignored) {
            }
        }

        LocalDateTime fromDt = filter.getFromDate() != null ? filter.getFromDate().atStartOfDay() : null;
        LocalDateTime toDt = filter.getToDate() != null ? filter.getToDate().atTime(23, 59, 59) : null;

        Page<Order> orderPage = orderRepository.findWithDynamicFilter(
                filter.getKeyword() != null ? filter.getKeyword().trim() : null,
                orderIdVal,
                filter.getStatus(),
                filter.getType(),
                filter.getPaymentStatus(),
                fromDt,
                toDt,
                pageable
        );

        List<OrderPosListResponse> list = orderPage.getContent().stream().map(order -> {
            Payment payment = paymentRepository.findByOrder_OrderId(order.getOrderId()).orElse(null);
            String pmName = payment != null && payment.getPaymentMethod() != null ? payment.getPaymentMethod().getName() : "Chưa thanh toán";
            String pStatus = payment != null && payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : PaymentStatus.PENDING.name();
            String cName = order.getUser() != null ? ((order.getUser().getFirstName() != null ? order.getUser().getFirstName() : "") + " " + (order.getUser().getLastName() != null ? order.getUser().getLastName() : "")).trim() : "Khách vãng lai";
            if (cName.isEmpty() && order.getUser() != null) cName = order.getUser().getUsername();
            String cPhone = order.getUser() != null ? order.getUser().getPhone() : "";

            return OrderPosListResponse.builder()
                    .orderId(order.getOrderId())
                    .customerName(cName)
                    .customerPhone(cPhone)
                    .tableId(order.getTable() != null ? order.getTable().getTableId() : null)
                    .orderType(order.getOrderType() != null ? order.getOrderType().name() : OrderType.COUNTER.name())
                    .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : OrderStatus.PENDING.name())
                    .paymentStatus(pStatus)
                    .paymentMethodName(pmName)
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .note(order.getNote())
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(list, pageable, orderPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPosDetailResponse getOrderDetail(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng #" + orderId));
        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        Payment payment = paymentRepository.findByOrder_OrderId(orderId).orElse(null);
        return mapToDetailResponse(order, details, payment);
    }

    @Override
    @Transactional
    public OrderPosDetailResponse updateOrderStatus(Integer orderId, UpdateStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng #" + orderId));
        if (request.getStatus() != null) {
            order.setOrderStatus(request.getStatus());
        }
        if (request.getNote() != null) {
            order.setNote(request.getNote());
        }
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Update items status if order is completed/cancelled
        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        if (request.getStatus() == OrderStatus.COMPLETED || request.getStatus() == OrderStatus.CANCELLED || request.getStatus() == OrderStatus.PREPARING || request.getStatus() == OrderStatus.READY) {
            OrderItemStatus itemStatus = OrderItemStatus.PENDING;
            if (request.getStatus() == OrderStatus.COMPLETED) itemStatus = OrderItemStatus.COMPLETED;
            else if (request.getStatus() == OrderStatus.CANCELLED) itemStatus = OrderItemStatus.CANCELLED;
            else if (request.getStatus() == OrderStatus.PREPARING) itemStatus = OrderItemStatus.PREPARING;
            else if (request.getStatus() == OrderStatus.READY) itemStatus = OrderItemStatus.COMPLETED;

            for (OrderDetail d : details) {
                d.setItemStatus(itemStatus);
                orderDetailRepository.save(d);
            }
        }

        // Không cần giải phóng bàn thủ công — khi order chuyển sang CANCELLED/COMPLETED,
        // findActiveOrderTableIds() sẽ tự động KHÔNG còn trả về tableId này nữa
        // (vì query chỉ lấy order có status IN PENDING/CONFIRMED/PREPARING),
        // nên bàn tự động hiện AVAILABLE ở lần gọi getPosTables() tiếp theo.

        Payment payment = paymentRepository.findByOrder_OrderId(orderId).orElse(null);
        return mapToDetailResponse(savedOrder, details, payment);
    }

    @Override
    @Transactional
    public OrderPosDetailResponse confirmPayment(Integer orderId, ConfirmPaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng #" + orderId));
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng này chưa có thông tin thanh toán"));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BusinessException("Đơn hàng này đã được thanh toán trước đó");
        }

        if (request.getPaymentMethodId() != null && request.getPaymentMethodId() > 0) {
            PaymentMethod pm = paymentMethodRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương thức thanh toán #" + request.getPaymentMethodId()));
            payment.setPaymentMethod(pm);
        }

        if (request.getTransactionRef() != null && !request.getTransactionRef().trim().isEmpty()) {
            payment.setTransactionRef(request.getTransactionRef().trim());
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Tự động gửi email hóa đơn ngay sau khi Cashier xác nhận thanh toán tiền mặt thành công
        try {
            invoiceService.resendInvoiceEmail(orderId);
        } catch (Exception e) {
            log.error("Không thể tự động gửi email hóa đơn cho đơn #{}: {}", orderId, e.getMessage(), e);
        }

        // Earn loyalty points if customer is present and points earned > 0
        if (order.getPointsEarned() != null && order.getPointsEarned() > 0 && order.getUser() != null) {
            LoyaltyPoint lp = LoyaltyPoint.builder()
                    .customer(order.getUser())
                    .transactionType(TransactionType.EARN)
                    .points(order.getPointsEarned())
                    .balanceAfter(getCustomerPoints(order.getUser().getUserId()) + order.getPointsEarned())
                    .referenceType(ReferenceType.ORDER)
                    .referenceId(order.getOrderId())
                    .note("Tích điểm từ thanh toán đơn #" + order.getOrderId())
                    .createdAt(LocalDateTime.now())
                    .build();
            loyaltyPointRepository.save(lp);
        }

        // If order was pending, confirm it
        if (order.getOrderStatus() == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        return mapToDetailResponse(order, details, payment);
    }

    private int getCustomerPoints(Integer customerId) {
        Integer pts = loyaltyPointRepository.getTotalPointsByCustomerId(customerId);
        return pts != null ? pts : 0;
    }

    private OrderPosDetailResponse mapToDetailResponse(Order order, List<OrderDetail> details, Payment payment) {
        String cName = order.getUser() != null ? ((order.getUser().getFirstName() != null ? order.getUser().getFirstName() : "") + " " + (order.getUser().getLastName() != null ? order.getUser().getLastName() : "")).trim() : "Khách vãng lai";
        if (cName.isEmpty() && order.getUser() != null) cName = order.getUser().getUsername();

        List<OrderPosDetailResponse.OrderItemPosResponse> itemResponses = details.stream().map(d ->
                OrderPosDetailResponse.OrderItemPosResponse.builder()
                        .itemId(d.getItemId())
                        .productId(d.getProduct() != null ? d.getProduct().getProductId() : null)
                        .productName(d.getProductNameSnapshot())
                        .variantName(d.getVariantNameSnapshot())
                        .price(d.getPriceSnapshot())
                        .quantity(d.getQuantity())
                        .itemTotal(d.getItemTotal())
                        .specialNote(d.getSpecialNote())
                        .build()
        ).collect(Collectors.toList());

        return OrderPosDetailResponse.builder()
                .orderId(order.getOrderId())
                .customerName(cName)
                .customerPhone(order.getUser() != null ? order.getUser().getPhone() : "")
                .customerEmail(order.getUser() != null ? order.getUser().getEmail() : "")
                .tableId(order.getTable() != null ? order.getTable().getTableId() : null)
                .orderType(order.getOrderType() != null ? order.getOrderType().name() : OrderType.COUNTER.name())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : OrderStatus.PENDING.name())
                .paymentStatus(payment != null && payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : PaymentStatus.PENDING.name())
                .paymentMethodId(payment != null && payment.getPaymentMethod() != null ? payment.getPaymentMethod().getPaymentMethodId() : null)
                .paymentMethodName(payment != null && payment.getPaymentMethod() != null ? payment.getPaymentMethod().getName() : "Chưa xác định")
                .transactionRef(payment != null ? payment.getTransactionRef() : "")
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .pointsEarned(order.getPointsEarned())
                .createdAt(order.getCreatedAt())
                .paidAt(payment != null ? payment.getPaidAt() : null)
                .note(order.getNote())
                .items(itemResponses)
                .build();
    }
}
