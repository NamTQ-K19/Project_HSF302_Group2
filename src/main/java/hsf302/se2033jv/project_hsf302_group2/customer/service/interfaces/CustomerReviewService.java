// customer/service/interfaces/CustomerReviewService.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.CreateReviewRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.ReviewResponse;

import java.util.List;

public interface CustomerReviewService {

    ReviewResponse createReview(Long userId, CreateReviewRequest request);

    List<ReviewResponse> getReviewsByCustomer(Long userId);

    List<ReviewResponse> getReviewsByOrder(Long userId, Integer orderId);

    boolean hasReviewed(Long userId, Integer orderId, Integer productId);

    List<ReviewResponse> getReviewableProducts(Long userId, Integer orderId);

    boolean isOrderFullyReviewed(Long userId, Integer orderId);
}