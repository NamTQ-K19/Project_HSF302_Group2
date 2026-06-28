package hsf302.se2033jv.project_hsf302_group2.common.exception;

import hsf302.se2033jv.project_hsf302_group2.auth.security.LoggedUser;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ErrorController {

    private final LoggedUser loggedUser;

    @GetMapping("/403")
    public String accessDenied(Model model) {
        String homeUrl = "/home"; // default cho guest/customer

        try {
            User user = loggedUser.getLoggedCustomer();
            if (user != null && user.getRole() != null) {
                String roleName = user.getRole().getRoleName().toUpperCase();
                switch (roleName) {
                    case "ADMIN":
                        homeUrl = "/admin/dashboard";
                        break;
                    case "MANAGER":
                        homeUrl = "/manager/reservations";
                        break;
                    case "CASHIER":
                        homeUrl = "/order/create";
                        break;
                    case "BARISTA":
                        homeUrl = "/order/edit";
                        break;
                    default:
                        homeUrl = "/home";
                }
            }
        } catch (Exception ignored) {
            // user chưa đăng nhập hoặc session hết hạn → về /home
        }

        model.addAttribute("homeUrl", homeUrl);
        return "error/403";
    }
}