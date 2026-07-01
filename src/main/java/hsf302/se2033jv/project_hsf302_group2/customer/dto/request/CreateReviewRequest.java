// customer/dto/request/CreateReviewRequest.java
package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import lombok.Data;

@Data
public class CreateReviewRequest {
    private Integer orderId;
    private Integer productId;
    private Integer rating;
    private String comment;
}