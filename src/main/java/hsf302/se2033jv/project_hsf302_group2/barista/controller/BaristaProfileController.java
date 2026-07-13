package hsf302.se2033jv.project_hsf302_group2.barista.controller;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.AccountResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/barista/profile")
@RequiredArgsConstructor
public class BaristaProfileController {

    private final AccountService accountService;

    @GetMapping
    public String profile(Model model, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Barista";
        AccountResponse user = accountService.getAccountByUsername(username);

        model.addAttribute("user", user);
        return "barista/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication != null ? authentication.getName() : "Barista";

        try {
            accountService.updateProfile(username, firstName, lastName, phone);
            redirectAttributes.addFlashAttribute("message", "Cập nhật hồ sơ thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            log.error("Error updating profile: ", e);
            redirectAttributes.addFlashAttribute("message", "Lỗi cập nhật: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/barista/profile";
    }

    @PostMapping("/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam(required = false) String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication != null ? authentication.getName() : "Barista";

        if (confirmPassword != null && !newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu xác nhận không khớp");
            redirectAttributes.addFlashAttribute("openPasswordModal", "true");
            return "redirect:/barista/profile";
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

        return "redirect:/barista/profile";
    }
}
