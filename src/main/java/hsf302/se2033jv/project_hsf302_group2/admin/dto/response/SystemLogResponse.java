package hsf302.se2033jv.project_hsf302_group2.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogResponse {
    private Integer logId;
    private Integer userId;
    private String username;
    private String action;
    private String targetType;
    private Integer targetId;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
}