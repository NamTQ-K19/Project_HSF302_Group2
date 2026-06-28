// customer/service/interfaces/CustomerReviewService.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.CreateReviewRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.ReviewResponse;

import java.util.List;

public interface CustomerReviewService {

    ReviewResponse createReview(Integer userId, CreateReviewRequest request);

    List<ReviewResponse> getReviewsByCustomer(Integer userId);

    List<ReviewResponse> getReviewsByOrder(Integer userId, Integer orderId);

    boolean hasReviewed(Integer userId, Integer orderId, Integer productId);

    List<ReviewResponse> getReviewableProducts(Integer userId, Integer orderId);

    boolean isOrderFullyReviewed(Integer userId, Integer orderId);
}