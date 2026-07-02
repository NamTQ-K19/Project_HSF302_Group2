// D:\SU26\HSF302\Practice\Project_HSF302_Group2\src\main\java\hsf302\se2033jv\project_hsf302_group2\admin\controller\AdminProfileController.java

package hsf302.se2033jv.project_hsf302_group2.admin.controller;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.AccountResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.ApiResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.AccountService;
import hsf302.se2033jv.project_hsf302_group2.common.util.AvatarStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AccountService accountService;
    private final AvatarStorageUtil avatarStorageUtil;  // ← THÊM VÀO

    @GetMapping
    public String profile(Model model, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Admin";
        AccountResponse user = accountService.getAccountByUsername(username);

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd 'Tháng' M, yyyy"));
        model.addAttribute("currentDate", currentDate);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Hồ sơ của tôi");

        return "admin/admin-profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication != null ? authentication.getName() : "Admin";

        try {
            accountService.updateProfile(username, firstName, lastName, phone);
            redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            log.error("Error updating profile: ", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam(required = false) String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication != null ? authentication.getName() : "Admin";

        if (confirmPassword != null && !newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu xác nhận không khớp");
            redirectAttributes.addFlashAttribute("openPasswordModal", "true");
            return "redirect:/admin/profile";
        }

        try {
            accountService.changePassword(username, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            log.error("Error changing password: ", e);
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
            redirectAttributes.addFlashAttribute("openPasswordModal", "true");
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        log.info("Uploading avatar for admin");

        String username = authentication != null ? authentication.getName() : "Admin";

        try {
            if (file == null || file.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Vui lòng chọn file ảnh");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/admin/profile";
            }

            AccountResponse currentUser = accountService.getAccountByUsername(username);
            if (currentUser != null && currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                avatarStorageUtil.delete(currentUser.getAvatarUrl());
            }

            String fileName = avatarStorageUtil.store(file);

            accountService.updateAvatar(username, fileName);

            redirectAttributes.addFlashAttribute("message", "Cập nhật ảnh đại diện thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            log.error("Error uploading avatar: ", e);
            redirectAttributes.addFlashAttribute("message", "Lỗi upload: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin/profile";
    }
}