package hsf302.se2033jv.project_hsf302_group2.common.config;

import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.ConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceModeInterceptor implements HandlerInterceptor {

    private final ConfigService configService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        // Bỏ qua nếu response không render view (VD: JSON API, redirect thẳng)
        if (modelAndView == null) {
            return;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/api/") || path.startsWith("/static/") || path.startsWith("/css/")
                || path.startsWith("/js/") || path.startsWith("/images/") || path.startsWith("/webjars/")
                || path.startsWith("/auth/login") || path.startsWith("/auth/forgot-password")
                || path.startsWith("/auth/register") || path.startsWith("/admin/")) {
            return;
        }

        if (configService.isMaintenanceMode()) {
            log.info("Maintenance mode is ON, redirecting to maintenance page for path: {}", path);
            modelAndView.setViewName("common/maintenance");
            modelAndView.getModel().clear();
            modelAndView.addObject("message", configService.getMaintenanceMessage());
        }
    }
}