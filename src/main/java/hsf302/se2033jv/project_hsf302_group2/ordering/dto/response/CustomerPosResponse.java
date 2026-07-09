package hsf302.se2033jv.project_hsf302_group2.ordering.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPosResponse {
    private Integer userId;
    private String fullName;
    private String phone;
    private String email;
    private Integer loyaltyPoints;
}
