package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Review;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ReviewRepository;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ReviewFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReviewResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ReviewManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewManageServiceImpl implements ReviewManageService {

    private final ReviewRepository reviewRepository;

    @Override
    public Page<ReviewResponse> getReviews(ReviewFilterRequest filter, int page, int size) {
        Boolean isVisible = null;
        if (filter.getVisibility() != null) {
            if ("visible".equalsIgnoreCase(filter.getVisibility())) isVisible = true;
            else if ("hidden".equalsIgnoreCase(filter.getVisibility())) isVisible = false;
        }

        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository
                .findWithDynamicFilter(filter.getKeyword(), filter.getProductId(), filter.getRating(), isVisible, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public void hideReview(Integer reviewId) {
        reviewRepository.updateVisibility(reviewId, false);
    }

    @Override
    public void showReview(Integer reviewId) {
        reviewRepository.updateVisibility(reviewId, true);
    }

    private ReviewResponse mapToResponse(Review r) {
        String customerName = r.getCustomer() != null
                ? ((r.getCustomer().getFirstName() != null ? r.getCustomer().getFirstName() : "") + " " +
                   (r.getCustomer().getLastName() != null ? r.getCustomer().getLastName() : "")).trim()
                : "Khách vãng lai";

        return ReviewResponse.builder()
                .reviewId(r.getReviewId())
                .customerName(customerName)
                .productName(r.getProduct() != null ? r.getProduct().getName() : "")
                .orderId(r.getOrder() != null ? r.getOrder().getOrderId() : null)
                .rating(r.getRating())
                .comment(r.getComment())
                .isVisible(r.getIsVisible())
                .pointsEarned(r.getPointsEarned())
                .createdAt(r.getCreatedAt())
                .build();
    }
}