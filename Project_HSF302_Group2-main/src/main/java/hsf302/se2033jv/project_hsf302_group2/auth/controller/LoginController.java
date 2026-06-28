package hsf302.se2033jv.project_hsf302_group2.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import hsf302.se2033jv.project_hsf302_group2.auth.security.CustomLoginSuccessHandler;

import java.io.IOException;

@Controller
public class LoginController {

    private final DaoAuthenticationProvider authenticationProvider;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public LoginController(DaoAuthenticationProvider authenticationProvider, CustomLoginSuccessHandler customLoginSuccessHandler) {
        this.authenticationProvider = authenticationProvider;
        this.customLoginSuccessHandler = customLoginSuccessHandler;
    }

    @GetMapping(path={"/login"})
    public String customerLogin(Model model,
                                @RequestParam(value = "error", required = false) String error) {
        if(error != null){
            if (error.equals("username")) {
                model.addAttribute("error", "Tài khoản không tồn tại.");
            } else if (error.equals("password")) {
                model.addAttribute("error", "Sai mật khẩu.");
            } else if (error.equals("unauthorized_staff")) {
                model.addAttribute("error", "Tài khoản nhân viên vui lòng đăng nhập tại cổng dành cho nhân viên.");
            } else {
                model.addAttribute("error", "Tài khoản hoặc mật khẩu không chính xác.");
            }
        }
        return  "account/customerLogin";
    }

    @GetMapping(path={"/staff/login"})
    public String staffLogin(Model model,
                             @RequestParam(value = "error", required = false) String error) {
        if(error != null){
            if (error.equals("unauthorized")) {
                model.addAttribute("error", "Tài khoản không có quyền truy cập trang quản lý.");
            } else if (error.equals("username")) {
                model.addAttribute("error", "Tài khoản không tồn tại.");
            } else if (error.equals("password")) {
                model.addAttribute("error", "Sai mật khẩu.");
            } else {
                model.addAttribute("error", "Tài khoản hoặc mật khẩu không chính xác.");
            }
        }
        return  "account/staffLogin";
    }

    @PostMapping("/staff/login")
    public String doPostStaffLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);

        try {
            Authentication auth = authenticationProvider.authenticate(authToken);
            
            // Check if user is only a Customer
            boolean isOnlyCustomer = true;
            for (GrantedAuthority authority : auth.getAuthorities()) {
                if (!authority.getAuthority().equals("ROLE_CUSTOMER")) {
                    isOnlyCustomer = false;
                    break;
                }
            }
            
            if (isOnlyCustomer) {
                // Clear context to prevent login
                SecurityContextHolder.clearContext();
                return "redirect:/staff/login?error=unauthorized";
            }
            
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            customLoginSuccessHandler.onAuthenticationSuccess(request, response, auth);
            return null;
        } catch (UsernameNotFoundException e) {
            request.getSession().setAttribute("SPRING_SECURITY_LAST_USERNAME", username);
            return "redirect:/staff/login?error=username";
        } catch (BadCredentialsException e) {
            request.getSession().setAttribute("SPRING_SECURITY_LAST_USERNAME", username);
            return "redirect:/staff/login?error=password";
        } catch (AuthenticationException | IOException | ServletException e) {
            System.err.println("Staff login failed: " + e.getMessage());
            e.printStackTrace();
            request.getSession().setAttribute("SPRING_SECURITY_LAST_USERNAME", username);
            return "redirect:/staff/login?error=true";
        }
    }
}