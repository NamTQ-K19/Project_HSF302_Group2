package hsf302.se2033jv.project_hsf302_group2.manager.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.LoyaltyPointResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CustomerRewardFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.CustomerRewardSummaryResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.CustomerRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/customers/rewards")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER')")
public class CustomerRewardController {

    private static final int PAGE_SIZE = 10;

    private final CustomerRewardService customerRewardService;
    private final ProfileService profileService;

    @GetMapping
    public String showSearchPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,      // ← THÊM MỚI: "points_asc" | "points_desc" | null
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User manager = profileService.getCurrentUser(auth.getName());

        Page<CustomerRewardSummaryResponse> customerPage =
                customerRewardService.searchCustomers(keyword, sort, page, PAGE_SIZE);

        model.addAttribute("manager", manager);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);                       // ← THÊM MỚI: để HTML biết trạng thái sort hiện tại
        model.addAttribute("customerPage", customerPage);
        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("activePage", "rewards");

        return "manager/customer/reward-search";
    }

    /** GET /manager/customers/rewards/{customerId}
     *  Bước: "Select Customer" → "Query loyalty_points..." → "Display reward points summary & history" */
    @GetMapping("/{customerId}")
    public String showCustomerRewardDetail(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User manager = profileService.getCurrentUser(auth.getName());

        CustomerRewardSummaryResponse summary = customerRewardService.getCustomerRewardSummary(customerId);
        CustomerRewardFilterRequest filter = new CustomerRewardFilterRequest();

        Page<LoyaltyPointResponse> transactionPage =
                customerRewardService.getTransactionHistory(customerId, filter, page, PAGE_SIZE);

        // Ghi log truy cập — bước cuối swimlane "Write system_logs"
        customerRewardService.logViewAccess(manager.getUserId(), customerId);

        model.addAttribute("manager", manager);
        model.addAttribute("summary", summary);
        model.addAttribute("filter", filter);
        model.addAttribute("transactionPage", transactionPage);
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("activePage", "rewards");

        return "manager/customer/reward-detail";
    }

    /** POST /manager/customers/rewards/{customerId}/filter
     *  Bước: "(Optional) Filter by date range or transaction type" → "Re-query loyalty_points" */
    @PostMapping("/{customerId}/filter")
    public String filterTransactionHistory(
            @PathVariable Integer customerId,
            @ModelAttribute("filter") CustomerRewardFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User manager = profileService.getCurrentUser(auth.getName());
        CustomerRewardSummaryResponse summary = customerRewardService.getCustomerRewardSummary(customerId);

        // Business Rule: From date phải <= To date
        if (filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {

            Page<LoyaltyPointResponse> transactionPage =
                    customerRewardService.getTransactionHistory(customerId, new CustomerRewardFilterRequest(), 0, PAGE_SIZE);

            model.addAttribute("manager", manager);
            model.addAttribute("summary", summary);
            model.addAttribute("filter", filter);
            model.addAttribute("transactionPage", transactionPage);
            model.addAttribute("transactions", transactionPage.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("pageSize", PAGE_SIZE);
            model.addAttribute("activePage", "rewards");
            model.addAttribute("dateError", "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");

            return "manager/customer/reward-detail";
        }

        Page<LoyaltyPointResponse> transactionPage =
                customerRewardService.getTransactionHistory(customerId, filter, page, PAGE_SIZE);

        model.addAttribute("manager", manager);
        model.addAttribute("summary", summary);
        model.addAttribute("filter", filter);
        model.addAttribute("transactionPage", transactionPage);
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("activePage", "rewards");

        return "manager/customer/reward-detail";
    }

    /** GET /manager/customers/rewards/{customerId}/clear-filter */
    @GetMapping("/{customerId}/clear-filter")
    public String clearFilter(@PathVariable Integer customerId) {
        return "redirect:/manager/customers/rewards/" + customerId;
    }
}
