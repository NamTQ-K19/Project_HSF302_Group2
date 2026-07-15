package hsf302.se2033jv.project_hsf302_group2.manager.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Integer reviewId;
    private String customerName;
    private String productName;
    private Integer orderId;
    private Integer rating;
    private String comment;
    private Boolean isVisible;
    private Integer pointsEarned;
    private LocalDateTime createdAt;
}