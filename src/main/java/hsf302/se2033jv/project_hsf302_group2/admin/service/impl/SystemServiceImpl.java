package hsf302.se2033jv.project_hsf302_group2.admin.service.impl;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.SystemLogResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.SystemService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemLog;
import hsf302.se2033jv.project_hsf302_group2.common.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {
    private final SystemLogRepository systemLogRepository;

    @Override
    public Page<SystemLogResponse> getSystemLogs(String keyword, String action, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.now().minusDays(30);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();

        if (effectiveStartDate.isAfter(effectiveEndDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Page<SystemLog> logsPage = systemLogRepository.findWithFilters(
                keyword, action, effectiveStartDate, effectiveEndDate, pageable);

        return logsPage.map(this::convertToResponse);
    }

    private SystemLogResponse convertToResponse(SystemLog systemLog) {
        if (systemLog == null) {
            return null;
        }

        return SystemLogResponse.builder()
                .logId(systemLog.getLogId())
                .userId(systemLog.getUser() != null ? systemLog.getUser().getUserId() : null)
                .username(systemLog.getUser() != null ? systemLog.getUser().getUsername() : null)
                .action(systemLog.getAction())
                .targetType(systemLog.getTargetType())
                .targetId(systemLog.getTargetId())
                .description(systemLog.getDescription())
                .ipAddress(systemLog.getIpAddress())
                .createdAt(systemLog.getCreatedAt())
                .formattedCreatedAt(formatDateTime(systemLog.getCreatedAt()))
                .build();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
