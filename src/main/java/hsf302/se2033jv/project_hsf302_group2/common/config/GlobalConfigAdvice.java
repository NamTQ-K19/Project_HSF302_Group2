package hsf302.se2033jv.project_hsf302_group2.common.config;

import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.ConfigService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalConfigAdvice {

    private final ConfigService configService;

    @ModelAttribute("siteConfig")
    public Map<String, String> addSiteConfigToModel() {
        return configService.getAllConfigs();
    }

    @ModelAttribute("siteName")
    public String getSiteName() {
        return configService.getSiteName();
    }

    @ModelAttribute("sitePhone")
    public String getSitePhone() {
        return configService.getSitePhone();
    }

    @ModelAttribute("siteEmail")
    public String getSiteEmail() {
        return configService.getSiteEmail();
    }

    @ModelAttribute("siteAddress")
    public String getSiteAddress() {
        return configService.getSiteAddress();
    }

    @ModelAttribute("siteHours")
    public String getSiteHours() {
        return configService.getSiteHours();
    }

    @ModelAttribute("siteLogo")
    public String getSiteLogo() {
        return configService.getSiteLogo();
    }

    @ModelAttribute("siteFavicon")
    public String getSiteFavicon() {
        return configService.getSiteFavicon();
    }

    @ModelAttribute("siteDescription")
    public String getSiteDescription() {
        return configService.getSiteDescription();
    }

    @ModelAttribute
    public void checkMaintenanceMode(HttpServletRequest request, ModelAndView modelAndView) {
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
            modelAndView.addObject("message", configService.getMaintenanceMessage());
        }
    }
}