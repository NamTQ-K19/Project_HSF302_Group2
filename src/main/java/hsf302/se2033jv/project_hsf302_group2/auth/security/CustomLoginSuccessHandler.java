package hsf302.se2033jv.project_hsf302_group2.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import hsf302.se2033jv.project_hsf302_group2.auth.security.CustomUserDetails;
import hsf302.se2033jv.project_hsf302_group2.auth.entity.User;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoggedUser loggedUser;

    public CustomLoginSuccessHandler(LoggedUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {


        if(authentication.getPrincipal() instanceof OidcUser){
            User user = loggedUser.getLoggedCustomer();
            if(user.getPhone() == null || user.getPhone().isEmpty() || 
               user.getUsername() == null || user.getUsername().equals(user.getEmail().split("@")[0])){
                request.getSession().setAttribute("profileReminder", "Welcome, please complete your account information.");
                response.sendRedirect("/profile/complete-google-account");
            } else {
                response.sendRedirect("/home");
            }
            return;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Prevent staff from logging in via the customer portal (/login)
        String uri = request.getRequestURI();
        boolean isStaff = authorities.stream().anyMatch(a -> !a.getAuthority().equals("ROLE_CUSTOMER"));
        
        if (isStaff && (uri.equals("/login") || uri.equals(request.getContextPath() + "/login"))) {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            response.sendRedirect("/login?error=unauthorized_staff");
            return;
        }

        String redirectUrl = "/home"; // default

        label:
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            switch (role) {
                case "ROLE_ADMIN":
                    redirectUrl = "/admin/dashboard";
                    break label;
                case "ROLE_MANAGER":
                    redirectUrl = "/dashboard";
                    break label;
                case "ROLE_CASHIER":
                    redirectUrl = "/order/create";
                    break label;
                case "ROLE_CUSTOMER":
                    redirectUrl = "/home";
                    break label;
                case "ROLE_BARISTA":
                    redirectUrl = "/order/edit";
                    break label;
            }
        }
        response.sendRedirect(redirectUrl);
    }
}
