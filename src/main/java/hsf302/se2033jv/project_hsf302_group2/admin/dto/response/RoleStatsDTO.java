package hsf302.se2033jv.project_hsf302_group2.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleStatsDTO {
    private String roleName;
    private Long count;
    private Double percentage;
}
