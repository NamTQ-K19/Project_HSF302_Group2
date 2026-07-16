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
            } else {
                model.addAttribute("error", "Tài khoản hoặc mật khẩu không chính xác.");
            }
        }
        return  "account/customerLogin";
    }

}