package hsf302.se2033jv.project_hsf302_group2.admin.controller;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.GeneralConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.ReservationConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.SystemConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemConfig;
import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.ConfigService;
import hsf302.se2033jv.project_hsf302_group2.common.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/config")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminConfigController {

    private final ConfigService configService;

    /**
     * Hiển thị trang cấu hình hệ thống
     */
    @GetMapping
    public String showConfigPage(Model model) {
        try {
            Map<String, List<SystemConfig>> groupedConfigs = configService.getGroupedConfigs();

            GeneralConfigRequest generalConfig = configService.getGeneralConfig();
            SystemConfigRequest systemConfig = configService.getSystemConfig();
            ReservationConfigRequest reservationConfig = configService.getReservationConfig();

            model.addAttribute("groupedConfigs", groupedConfigs);
            model.addAttribute("configGroups", groupedConfigs.keySet());
            model.addAttribute("generalConfig", generalConfig);
            model.addAttribute("systemConfig", systemConfig);
            model.addAttribute("reservationConfig", reservationConfig);
            model.addAttribute("criticalError", false);   // MỚI

        } catch (Exception e) {
            // EX1: load config từ DB thất bại
            log.error("Critical error loading system configuration: {}", e.getMessage(), e);
            model.addAttribute("criticalError", true);   // MỚI
            model.addAttribute("criticalErrorMessage",
                    "Critical Error: Failed to load system configuration properties. "
                            + "Please refresh the page or contact the technical team.");   // MỚI
            // Không đưa groupedConfigs vào model → PHẦN 3 (config cards) tự động ẩn
            // nhờ th:if có sẵn: groupedConfigs != null and !groupedConfigs.isEmpty()
        }

        return "admin/system-config";
    }

    // GENERAL CONFIG
    /**
     * Cập nhật cấu hình chung
     */
    @PostMapping("/general/update")
    public String updateGeneralConfig(
            @Valid @ModelAttribute("generalConfig") GeneralConfigRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Vui lòng kiểm tra lại thông tin: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/config";
        }

        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();   // MỚI
            configService.updateGeneralConfig(request, adminId);             // sửa: thêm adminId
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật cấu hình chung thành công!");
        } catch (Exception e) {
            log.error("Error updating general config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    /**
     * Reset cấu hình chung về mặc định
     */
    @PostMapping("/general/reset")
    public String resetGeneralConfig(RedirectAttributes redirectAttributes) {
        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();

            // Reset từng config
            configService.resetConfig("site_name", adminId);
            configService.resetConfig("site_phone", adminId);
            configService.resetConfig("site_email", adminId);
            configService.resetConfig("site_address", adminId);
            configService.resetConfig("site_hours", adminId);
            configService.resetConfig("site_logo", adminId);
            configService.resetConfig("site_favicon", adminId);
            configService.resetConfig("site_description", adminId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã reset tất cả cấu hình chung về giá trị mặc định!");
        } catch (Exception e) {
            log.error("Error resetting general config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Reset thất bại: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    // SYSTEM CONFIG
    /**
     * Cập nhật cấu hình hệ thống
     */
    @PostMapping("/system/update")
    public String updateSystemConfig(
            @Valid @ModelAttribute("systemConfig") SystemConfigRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Vui lòng kiểm tra lại thông tin: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/config";
        }

        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();
            configService.updateSystemConfig(request, adminId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật cấu hình hệ thống thành công!");
        } catch (Exception e) {
            log.error("Error updating system config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/admin/config";
    }

    /**
     * Reset cấu hình hệ thống về mặc định
     */
    @PostMapping("/system/reset")
    public String resetSystemConfig(RedirectAttributes redirectAttributes) {
        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();

            configService.resetConfig("maintenance_mode", adminId);
            configService.resetConfig("maintenance_message", adminId);
            configService.resetConfig("log_retention_days", adminId);
            configService.resetConfig("default_language", adminId);
            configService.resetConfig("items_per_page", adminId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã reset tất cả cấu hình hệ thống về giá trị mặc định!");
        } catch (Exception e) {
            log.error("Error resetting system config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Reset thất bại: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    // RESERVATION CONFIG
    /**
     * Cập nhật cấu hình đặt bàn
     */
    @PostMapping("/reservation/update")
    public String updateReservationConfig(
            @Valid @ModelAttribute("reservationConfig") ReservationConfigRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Vui lòng kiểm tra lại thông tin: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/config";
        }

        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();
            configService.updateReservationConfig(request, adminId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật cấu hình đặt bàn thành công!");
        } catch (Exception e) {
            log.error("Error updating reservation config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/admin/config";
    }

    /**
     * Reset cấu hình đặt bàn về mặc định
     */
    @PostMapping("/reservation/reset")
    public String resetReservationConfig(RedirectAttributes redirectAttributes) {
        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();

            configService.resetConfig("reservation_deposit_amount", adminId);
            configService.resetConfig("reservation_hold_minutes", adminId);
            configService.resetConfig("reservation_max_per_day", adminId);
            configService.resetConfig("reservation_max_advance_days", adminId);
            configService.resetConfig("reservation_min_advance_hours", adminId);
            configService.resetConfig("reservation_max_party_size", adminId);
            configService.resetConfig("reservation_cancel_before_minutes", adminId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã reset tất cả cấu hình đặt bàn về giá trị mặc định!");
        } catch (Exception e) {
            log.error("Error resetting reservation config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Reset thất bại: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    // LEGACY METHODS (giữ lại để tương thích với API cũ)
    /**
     * Cập nhật một config (legacy)
     */
    @PostMapping("/update")
    public String updateConfig(
            @RequestParam String configKey,
            @RequestParam String configValue,
            RedirectAttributes redirectAttributes) {

        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();
            configService.updateConfig(configKey, configValue, adminId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật cấu hình thành công!");
        } catch (Exception e) {
            log.error("Error updating config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/admin/config";
    }

    /**
     * Cập nhật nhiều config cùng lúc (legacy)
     */
    @PostMapping("/update-batch")
    public String updateBatchConfig(
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {

        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();
            int updatedCount = configService.updateBatchConfigs(allParams, adminId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thành công " + updatedCount + " cấu hình!");
        } catch (Exception e) {
            log.error("Error updating batch config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/admin/config";
    }

    /**
     * Reset config về giá trị mặc định (legacy)
     */
    @PostMapping("/reset")
    public String resetConfig(
            @RequestParam String configKey,
            RedirectAttributes redirectAttributes) {

        try {
            Integer adminId = SecurityUtils.getCurrentUserId().intValue();
            configService.resetConfig(configKey, adminId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã reset cấu hình " + configKey + " về giá trị mặc định!");
        } catch (Exception e) {
            log.error("Error resetting config: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Reset thất bại: " + e.getMessage());
        }

        return "redirect:/admin/config";
    }
}