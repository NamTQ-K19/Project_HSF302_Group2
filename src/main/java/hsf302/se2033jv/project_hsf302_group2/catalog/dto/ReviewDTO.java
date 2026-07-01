package hsf302.se2033jv.project_hsf302_group2.catalog.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for a customer review displayed on product detail page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private String customerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
