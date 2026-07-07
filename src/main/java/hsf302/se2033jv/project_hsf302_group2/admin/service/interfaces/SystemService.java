package hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.SystemLogResponse;
import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemService {
    Page<SystemLogResponse> getSystemLogs(String keyword, String action, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
