// customer/dto/response/ReviewResponse.java
package hsf302.se2033jv.project_hsf302_group2.customer.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Integer reviewId;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private String productImage;
    private Integer rating;
    private String comment;
    private Integer pointsEarned;
    private Boolean isVisible;
    private LocalDateTime createdAt;
}