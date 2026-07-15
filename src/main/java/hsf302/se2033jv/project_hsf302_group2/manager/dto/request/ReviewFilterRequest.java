package hsf302.se2033jv.project_hsf302_group2.manager.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFilterRequest {
    private String keyword;      // Tên khách hàng HOẶC nội dung
    private Integer productId;
    private Integer rating;
    private String visibility;
}