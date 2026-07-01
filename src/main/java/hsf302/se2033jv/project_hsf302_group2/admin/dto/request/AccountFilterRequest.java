package hsf302.se2033jv.project_hsf302_group2.admin.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountFilterRequest {
    private String role;
    private String status;
    private String keyword;
}