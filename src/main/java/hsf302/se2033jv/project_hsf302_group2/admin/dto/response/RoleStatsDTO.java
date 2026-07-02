package hsf302.se2033jv.project_hsf302_group2.admin.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleStatsDTO {
    private String roleName;
    private Long count;
    private Double percentage;
}
