package hsf302.se2033jv.project_hsf302_group2.manager.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRewardSummaryResponse {
    private Integer customerId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private Boolean status;
    private Integer currentBalance;
    private Integer totalEarned;
    private Integer totalRedeemed;
}
