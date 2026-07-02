package hsf302.se2033jv.project_hsf302_group2.admin.controller;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.AccountFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.CreateInternalAccountRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.OtpVerificationRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.AccountResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.ChartDataResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.DashboardStatsResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.PageResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.AccountService;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.DashboardService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AccountService accountService;
    private final DashboardService dashboardService;

    // ===================== VIEW PAGES =====================

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        log.info("Loading dashboard page");

        try {
            // ===== DỮ LIỆU CŨ (GIỮ NGUYÊN) =====
            DashboardStatsResponse stats = dashboardService.getDashboardStats();
            ChartDataResponse chartData = dashboardService.getChartData();

            String username = authentication != null ? authentication.getName() : "Admin";
            String displayName = username;
            if (authentication != null) {
                AccountResponse user = accountService.getAccountByUsername(username);
                if (user != null) {
                    displayName = user.getFirstName() + " "  + user.getLastName();
                }
            }
            model.addAttribute("displayName", displayName);

            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd 'Tháng' M, yyyy"));
            int hour = LocalDateTime.now().getHour();
            String greeting;
            String greetingIcon;
            String iconColor;

            if (hour < 12) {
                greeting = "Chào buổi sáng";
                greetingIcon = "fa-sun";
                iconColor = "#f59e0b";
            } else if (hour < 17) {
                greeting = "Chào buổi chiều";
                greetingIcon = "fa-sun";
                iconColor = "#f59e0b";
            } else {
                greeting = "Chào buổi tối";
                greetingIcon = "fa-moon";
                iconColor = "#6366f1";
            }

            model.addAttribute("greeting", greeting);
            model.addAttribute("greetingIcon", greetingIcon);
            model.addAttribute("iconColor", iconColor);
            model.addAttribute("pageTitle", "Tổng quan");

            // ===== 1. THỐNG KÊ NGƯỜI DÙNG =====
            model.addAttribute("totalUsers", dashboardService.getTotalUsers());      // 1. Tổng tài khoản
            model.addAttribute("newUsersToday", dashboardService.getNewUsersToday()); // 2. Mới hôm nay
            model.addAttribute("activeUsers", dashboardService.getActiveUsers());    // 3. Đang hoạt động
            model.addAttribute("lockedUsers", dashboardService.getLockedUsers());    // (thêm)

            // ===== 5. PHÂN BỐ THEO VAI TRÒ =====
            model.addAttribute("roleStats", dashboardService.getRoleStats());

            // ===== 6. HOẠT ĐỘNG GẦN ĐÂY =====
            model.addAttribute("recentLogs", dashboardService.getRecentLogs());

            // ===== DỮ LIỆU CŨ (GIỮ NGUYÊN) =====
            if (stats.getSalesStats() != null) {
                model.addAttribute("totalSales", formatCurrency(stats.getSalesStats().getTotalSalesToday()));
                model.addAttribute("salesChange", formatChange(stats.getSalesStats().getSalesChangePercent()));
                model.addAttribute("ordersCompleted", stats.getSalesStats().getOrdersCompleted());
                model.addAttribute("ordersChange", formatChange(stats.getSalesStats().getOrdersChangePercent()));
                model.addAttribute("topSellingItem", stats.getSalesStats().getTopSellingItem());
                model.addAttribute("topSellingCount", stats.getSalesStats().getTopSellingItemCount() + " bán hôm nay");
            }

            if (stats.getStaffStats() != null) {
                model.addAttribute("activeStaff", stats.getStaffStats().getActiveStaff());
                model.addAttribute("staffStatus", stats.getStaffStats().getStatus());
            }

            if (stats.getRecentOrders() != null && !stats.getRecentOrders().isEmpty()) {
                model.addAttribute("recentOrders", stats.getRecentOrders());
                model.addAttribute("orderCount", stats.getRecentOrders().size());
            } else {
                model.addAttribute("recentOrders", new ArrayList<>());
                model.addAttribute("orderCount", 0);
            }

            if (stats.getInventoryAlerts() != null && !stats.getInventoryAlerts().isEmpty()) {
                model.addAttribute("inventoryAlerts", stats.getInventoryAlerts());
                model.addAttribute("alertCount", stats.getInventoryAlerts().size());
            } else {
                model.addAttribute("inventoryAlerts", new ArrayList<>());
                model.addAttribute("alertCount", 0);
            }

            if (stats.getAiInsights() != null && !stats.getAiInsights().isEmpty()) {
                model.addAttribute("aiInsights", stats.getAiInsights());
            } else {
                model.addAttribute("aiInsights", new ArrayList<>());
            }

            if (chartData != null) {
                model.addAttribute("chartLabels", chartData.getLabels());
                model.addAttribute("chartSalesData", chartData.getSalesData());
                model.addAttribute("chartOrderCounts", chartData.getOrderCounts());
            } else {
                model.addAttribute("chartLabels", new ArrayList<>());
                model.addAttribute("chartSalesData", new ArrayList<>());
                model.addAttribute("chartOrderCounts", new ArrayList<>());
            }

        } catch (Exception e) {
            log.error("Error loading dashboard: ", e);
            setDefaultModelAttributes(model);
        }

        return "admin/dashboard";
    }

    @GetMapping("/accounts")
    public String accountList(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        log.info("Loading account list page");

        try {
            AccountFilterRequest filterRequest = AccountFilterRequest.builder()
                    .role(role)
                    .status(status)
                    .keyword(keyword)
                    .build();

            Pageable pageable = PageRequest.of(page, 10, Sort.by("userId").descending());
            PageResponse<?> pageResponse = accountService.getAccounts(filterRequest, pageable);

            model.addAttribute("accounts", pageResponse.getContent());
            model.addAttribute("currentPage", pageResponse.getPageNumber());
            model.addAttribute("totalPages", pageResponse.getTotalPages());
            model.addAttribute("totalElements", pageResponse.getTotalElements());
            model.addAttribute("filterRequest", filterRequest);
            model.addAttribute("roleParam", role != null ? role : "");
            model.addAttribute("statusParam", status != null ? status : "");
            model.addAttribute("keywordParam", keyword != null ? keyword : "");

        } catch (Exception e) {
            log.error("Error loading accounts: ", e);
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("totalElements", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 0);
            model.addAttribute("filterRequest", new AccountFilterRequest());
            model.addAttribute("roleParam", "");
            model.addAttribute("statusParam", "");
            model.addAttribute("keywordParam", "");
        }

        return "admin/accounts";
    }

    @GetMapping("/accounts/create")
    public String createAccountForm(Model model) {
        model.addAttribute("request", new CreateInternalAccountRequest());
        model.addAttribute("pageTitle", "Tạo tài khoản mới");
        return "admin/create-account";
    }

    @PostMapping("/accounts/create")
    public String createAccountSubmit(@Valid @ModelAttribute("request") CreateInternalAccountRequest request,
                                      RedirectAttributes redirectAttributes) {
        log.info("Creating internal account for email: {}", request.getEmail());
        try {
            accountService.createInternalAccount(request);
            redirectAttributes.addAttribute("email", request.getEmail());
            return "redirect:/admin/accounts/verify-otp";
        } catch (Exception e) {
            log.error("Error creating account: ", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/accounts/create";
        }
    }

    // ===== GET - HIỂN THỊ FORM OTP =====
    @GetMapping("/accounts/verify-otp")
    public String verifyOtpForm(@RequestParam String email,
                                @RequestParam(required = false) String error,
                                Model model) {
        model.addAttribute("email", email);
        model.addAttribute("adminEmail", "luugiang205@gmail.com");
        model.addAttribute("request", new OtpVerificationRequest());
        model.addAttribute("pageTitle", "Xác thực OTP");
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/verify-otp";
    }

    // ===== POST - XỬ LÝ VERIFY OTP (CHỈ CÓ 1 METHOD NÀY) =====
    @PostMapping("/accounts/verify-otp")
    @ResponseBody
    public Map<String, Object> verifyOtpSubmit(@RequestParam String email,
                                               @RequestParam String otpCode) {
        log.info("Verifying OTP for email: {}", email);
        Map<String, Object> response = new HashMap<>();
        try {
            OtpVerificationRequest request = new OtpVerificationRequest();
            request.setEmail(email);
            request.setOtpCode(otpCode);
            accountService.verifyOtpAndCreateAccount(request);
            response.put("success", true);
            response.put("message", "Tài khoản đã được tạo thành công!");
            return response;
        } catch (Exception e) {
            log.error("Error verifying OTP: ", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    // ===== GET - RESEND OTP =====
    @GetMapping("/accounts/resend-otp")
    public String resendOtp(@RequestParam String email,
                            RedirectAttributes redirectAttributes) {
        log.info("Resending OTP for email: {}", email);
        try {
            accountService.resendOtp(email);
            redirectAttributes.addFlashAttribute("message", "Mã OTP mới đã được gửi đến email của bạn");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            log.error("Error resending OTP: ", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        redirectAttributes.addAttribute("email", email);
        return "redirect:/admin/accounts/verify-otp";
    }

    // ===== POST - RESEND OTP (CHO AJAX) =====
    @PostMapping("/accounts/resend-otp")
    @ResponseBody
    public Map<String, Object> resendOtpSubmit(@RequestParam String email) {
        log.info("Resending OTP for email: {}", email);
        Map<String, Object> response = new HashMap<>();
        try {
            accountService.resendOtp(email);
            response.put("success", true);
            response.put("message", "Mã OTP mới đã được gửi đến email của bạn");
            return response;
        } catch (Exception e) {
            log.error("Error resending OTP: ", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    @GetMapping("/accounts/profile/{userId}")
    public String accountProfile(@PathVariable Integer userId, Model model) {
        try {
            var account = accountService.getAccountById(userId);
            model.addAttribute("account", account);
            model.addAttribute("pageTitle", "Hồ sơ người dùng");
        } catch (Exception e) {
            log.error("Error loading account profile: ", e);
            model.addAttribute("error", "Không tìm thấy người dùng");
        }
        return "admin/account-profile";
    }

    @GetMapping("/accounts/toggle/{userId}")
    public String toggleAccountStatus(
            @PathVariable Integer userId,
            @RequestParam boolean lock,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        log.info("Toggling account status for user: {}, lock: {}, reason: {}", userId, lock, reason);

        try {
            if (lock) {
                accountService.lockAccount(userId, reason);
                redirectAttributes.addFlashAttribute("message", "Tài khoản đã bị khóa thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                accountService.unlockAccount(userId, reason);
                redirectAttributes.addFlashAttribute("message", "Tài khoản đã được mở khóa thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            }
        } catch (Exception e) {
            log.error("Error toggling account status: ", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        String referer = request.getHeader("Referer");
        if (referer == null || referer.trim().isEmpty()) {
            referer = "/admin/accounts";
        }
        return "redirect:" + referer;
    }

    // ===================== HELPER METHODS =====================

    private void setDefaultModelAttributes(Model model) {
        model.addAttribute("greeting", "Chào buổi chiều, John! ☀️");
        model.addAttribute("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd 'Tháng' M, yyyy")));
        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("totalSales", "đ0");
        model.addAttribute("salesChange", "↑ 0% so với hôm qua");
        model.addAttribute("ordersCompleted", 0);
        model.addAttribute("ordersChange", "↑ 0% so với hôm qua");
        model.addAttribute("topSellingItem", "Chưa có");
        model.addAttribute("topSellingCount", "0 bán hôm nay");
        model.addAttribute("activeStaff", 0);
        model.addAttribute("staffStatus", "Đang tải...");
        model.addAttribute("recentOrders", new ArrayList<>());
        model.addAttribute("orderCount", 0);
        model.addAttribute("inventoryAlerts", new ArrayList<>());
        model.addAttribute("alertCount", 0);
        model.addAttribute("aiInsights", new ArrayList<>());
        model.addAttribute("chartLabels", new ArrayList<>());
        model.addAttribute("chartSalesData", new ArrayList<>());
        model.addAttribute("chartOrderCounts", new ArrayList<>());
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "đ0";
        return "đ" + String.format("%,d", amount.longValue());
    }

    private String formatChange(BigDecimal change) {
        if (change == null) return "↑ 0% so với hôm qua";
        String arrow = change.compareTo(BigDecimal.ZERO) >= 0 ? "↑" : "↓";
        return arrow + " " + String.format("%.1f", Math.abs(change.doubleValue())) + "% so với hôm qua";
    }

    private String formatChange(Double change) {
        if (change == null) return "↑ 0% so với hôm qua";
        String arrow = change >= 0 ? "↑" : "↓";
        return arrow + " " + String.format("%.1f", Math.abs(change)) + "% so với hôm qua";
    }
}