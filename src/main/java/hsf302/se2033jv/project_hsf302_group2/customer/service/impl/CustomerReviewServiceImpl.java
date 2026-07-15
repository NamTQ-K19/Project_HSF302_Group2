// customer/service/impl/CustomerReviewServiceImpl.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.LoyaltyPoint;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Product;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Review;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.*;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.*;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.CreateReviewRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.ReviewResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerReviewServiceImpl implements CustomerReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LoyaltyPointRepository loyaltyPointRepository;
    private final PolicyRepository policyRepository;

    private static final String DEFAULT_IMAGE = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='60' viewBox='0 0 60 60'%3E%3Crect width='60' height='60' fill='%23f8f9fa'/%3E%3Ctext x='50%25' y='55%25' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='30' fill='%23dee2e6'%3E☕%3C/text%3E%3C/svg%3E";

    @Override
    @Transactional
    public ReviewResponse createReview(Integer userId, CreateReviewRequest request) {
        log.info("Creating review for user: {}, order: {}, product: {}", userId, request.getOrderId(), request.getProductId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException("You are not authorized to review this order");
        }

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("Only completed orders can be reviewed");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByCustomer_UserIdAndOrder_OrderIdAndProduct_ProductId(
                userId, request.getOrderId(), request.getProductId())) {
            throw new BusinessException("You have already reviewed this product");
        }

        boolean productInOrder = orderDetailRepository.findByOrder_OrderId(order.getOrderId()).stream()
                .anyMatch(detail -> detail.getProduct().getProductId().equals(request.getProductId()));
        if (!productInOrder) {
            throw new BusinessException("This product is not in your order");
        }

        int reviewPoints = getReviewEarnPoints();   // đọc động từ policies

        Review review = Review.builder()
                .customer(user)
                .order(order)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .isVisible(true)
                .pointsEarned(reviewPoints)
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Chỉ ghi giao dịch điểm nếu chính sách đang active và giá trị > 0
        if (reviewPoints > 0) {
            Integer currentPoints = getCurrentPoints(userId);
            LoyaltyPoint loyaltyPoint = LoyaltyPoint.builder()
                    .customer(user)
                    .transactionType(TransactionType.EARN)
                    .points(reviewPoints)
                    .balanceAfter(currentPoints + reviewPoints)
                    .referenceType(ReferenceType.REVIEW)
                    .referenceId(savedReview.getReviewId())
                    .note("Tích điểm từ đánh giá sản phẩm")
                    .createdAt(LocalDateTime.now())
                    .build();
            loyaltyPointRepository.save(loyaltyPoint);
        }

        return buildReviewResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByCustomer(Integer userId) {
        List<Review> reviews = reviewRepository.findByCustomer_UserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::buildReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByOrder(Integer userId, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException("You are not authorized to view reviews for this order");
        }

        List<Review> reviews = reviewRepository.findByCustomer_UserIdAndOrder_OrderId(userId, orderId);
        return reviews.stream()
                .map(this::buildReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasReviewed(Integer userId, Integer orderId, Integer productId) {
        return reviewRepository.existsByCustomer_UserIdAndOrder_OrderIdAndProduct_ProductId(
                userId, orderId, productId);
    }

    @Override
    public List<ReviewResponse> getReviewableProducts(Integer userId, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException("You are not authorized to view this order");
        }

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            return new ArrayList<>();
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);

        List<ReviewResponse> reviewableProducts = new ArrayList<>();
        for (OrderDetail detail : details) {
            Product product = detail.getProduct();
            boolean alreadyReviewed = reviewRepository.existsByCustomer_UserIdAndOrder_OrderIdAndProduct_ProductId(
                    userId, orderId, product.getProductId());

            if (!alreadyReviewed) {
                ReviewResponse response = new ReviewResponse();
                response.setProductId(product.getProductId());
                response.setProductName(product.getName());

                String imageUrl = DEFAULT_IMAGE;
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    if (product.getImages().get(0) != null) {
                        imageUrl = product.getImages().get(0).getImageUrl();
                    }
                }
                response.setProductImage(imageUrl);
                response.setOrderId(orderId);

                reviewableProducts.add(response);
            }
        }

        return reviewableProducts;
    }

    @Override
    public boolean isOrderFullyReviewed(Integer userId, Integer orderId) {
        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);

        for (OrderDetail detail : details) {
            boolean reviewed = reviewRepository.existsByCustomer_UserIdAndOrder_OrderIdAndProduct_ProductId(
                    userId, orderId, detail.getProduct().getProductId());
            if (!reviewed) {
                return false;
            }
        }
        return true;
    }

    // ==================== PRIVATE METHODS ====================

    private ReviewResponse buildReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setOrderId(review.getOrder().getOrderId());
        response.setProductId(review.getProduct().getProductId());
        response.setProductName(review.getProduct().getName());

        String imageUrl = DEFAULT_IMAGE;
        if (review.getProduct().getImages() != null && !review.getProduct().getImages().isEmpty()) {
            if (review.getProduct().getImages().get(0) != null) {
                imageUrl = review.getProduct().getImages().get(0).getImageUrl();
            }
        }
        response.setProductImage(imageUrl);

        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setPointsEarned(review.getPointsEarned());
        response.setIsVisible(review.getIsVisible());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }

    private Integer getCurrentPoints(Integer customerId) {
        Integer points = loyaltyPointRepository.getTotalPointsByCustomerId(customerId);
        return points != null ? points : 0;
    }

    /**
     * Đọc số điểm tặng cố định mỗi lượt review từ policies (EARN + REVIEW).
     * Trả về 0 nếu chính sách không tồn tại hoặc đang bị vô hiệu hóa.
     */
    private Integer getReviewEarnPoints() {
        return policyRepository.findByPolicyTypeAndActionType(PolicyType.EARN, PolicyActionType.REVIEW)
                .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                .map(p -> p.getCurrencyValue().intValue())
                .orElse(0);
    }
}