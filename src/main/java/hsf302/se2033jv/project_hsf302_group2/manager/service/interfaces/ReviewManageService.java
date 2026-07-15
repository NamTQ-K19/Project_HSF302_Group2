package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ReviewFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;

public interface ReviewManageService {

    Page<ReviewResponse> getReviews(ReviewFilterRequest filter, int page, int size);

    void hideReview(Integer reviewId);

    void showReview(Integer reviewId);
}
