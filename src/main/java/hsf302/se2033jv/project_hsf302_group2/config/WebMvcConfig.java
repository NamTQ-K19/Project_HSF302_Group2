package hsf302.se2033jv.project_hsf302_group2.config;

import hsf302.se2033jv.project_hsf302_group2.common.config.MaintenanceModeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class    WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.avatar-dir}")
    private String avatarDir;

    @Value("${app.upload.avatar-url-prefix}")
    private String avatarUrlPrefix;

    private final MaintenanceModeInterceptor maintenanceModeInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(avatarDir);
        String absolutePath = uploadPath.toFile().getAbsolutePath();

        registry.addResourceHandler(avatarUrlPrefix + "/**")
                .addResourceLocations("file:///" + absolutePath + "/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/home");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(maintenanceModeInterceptor);
    }
}
