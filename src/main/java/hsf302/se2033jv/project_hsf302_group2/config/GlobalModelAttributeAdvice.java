package hsf302.se2033jv.project_hsf302_group2.config;

import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.IHomepageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private final IHomepageService homepageService;

    @ModelAttribute("categories")
    public Object populateCategories() {
        return homepageService.getActiveCategories();
    }
}
