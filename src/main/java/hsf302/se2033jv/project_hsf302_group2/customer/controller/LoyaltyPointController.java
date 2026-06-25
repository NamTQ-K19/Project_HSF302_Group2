package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.LoyaltyPointResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.LoyaltyPointFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.LoyaltyPointService;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer/points")
@RequiredArgsConstructor
public class LoyaltyPointController {

    private static final int PAGE_SIZE = 10;

    private final LoyaltyPointService loyaltyPointService;
    private final ProfileService profileService;

    @GetMapping
    public String showLoyaltyPoints(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User user = profileService.getCurrentUser(auth.getName());
        Integer customerId = user.getUserId();

        int currentBalance = loyaltyPointService.getCurrentBalance(customerId);
        Page<LoyaltyPointResponse> transactionPage = loyaltyPointService.getHistory(customerId, page, PAGE_SIZE);

        model.addAttribute("user", user);
        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("transactionPage", transactionPage);
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("filter", new LoyaltyPointFilterRequest());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);

        return "customer/loyalty/points";
    }

    @PostMapping("/filter")
    public String filterLoyaltyPoints(
            @ModelAttribute("filter") LoyaltyPointFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User user = profileService.getCurrentUser(auth.getName());
        Integer customerId = user.getUserId();

        if (filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {

            int currentBalance = loyaltyPointService.getCurrentBalance(customerId);
            Page<LoyaltyPointResponse> transactionPage = loyaltyPointService.getHistory(customerId, 0, PAGE_SIZE);

            model.addAttribute("user", user);
            model.addAttribute("currentBalance", currentBalance);
            model.addAttribute("transactionPage", transactionPage);
            model.addAttribute("transactions", transactionPage.getContent());
            model.addAttribute("filter", filter);
            model.addAttribute("currentPage", 0);
            model.addAttribute("pageSize", PAGE_SIZE);
            model.addAttribute("dateError", "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");
            return "customer/loyalty/points";
        }

        int currentBalance = loyaltyPointService.getCurrentBalance(customerId);
        Page<LoyaltyPointResponse> transactionPage = loyaltyPointService.getHistoryWithFilter(customerId, filter, page, PAGE_SIZE);

        model.addAttribute("user", user);
        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("transactionPage", transactionPage);
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);

        return "customer/loyalty/points";
    }

    @GetMapping("/clear-filter")
    public String clearFilter() {
        return "redirect:/customer/points";
    }
}

