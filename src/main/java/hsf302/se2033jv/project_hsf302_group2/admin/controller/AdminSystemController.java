package hsf302.se2033jv.project_hsf302_group2.admin.controller;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.SystemLogResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.SystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSystemController {
    private final SystemService systemLogService;

    @GetMapping("/system-logs")
    public String getSystemLogs(
            Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        try {
            if (pageable.getPageSize() > 100) {
                pageable = PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort());
            }

            Page<SystemLogResponse> logsPage = systemLogService.getSystemLogs(
                    keyword, action, startDate, endDate, pageable);

            model.addAttribute("logs", logsPage.getContent());
            model.addAttribute("currentPage", logsPage.getNumber());
            model.addAttribute("totalPages", logsPage.getTotalPages());
            model.addAttribute("totalElements", logsPage.getTotalElements());
            model.addAttribute("pageSize", logsPage.getSize());

            model.addAttribute("keyword", keyword);
            model.addAttribute("action", action);
            model.addAttribute("startDate", startDate != null ? startDate.toString() : null);
            model.addAttribute("endDate", endDate != null ? endDate.toString() : null);

        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Database error while retrieving system logs: {}", e.getMessage());
            model.addAttribute("logs", Collections.emptyList());
            model.addAttribute("totalElements", 0);
            model.addAttribute("error", "Internal System Error. Unable to connect to the log database. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error while retrieving system logs: {}", e.getMessage());
            model.addAttribute("logs", Collections.emptyList());
            model.addAttribute("totalElements", 0);
            model.addAttribute("error", "Internal System Error. Unable to retrieve logs. Please try again later.");
        }

        return "admin/system-logs";
    }
}
