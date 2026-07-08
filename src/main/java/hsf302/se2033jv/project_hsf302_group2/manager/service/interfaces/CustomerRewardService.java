package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.LoyaltyPointResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CustomerRewardFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.CustomerRewardSummaryResponse;
import org.springframework.data.domain.Page;

public interface CustomerRewardService {

    // Bước "Search / Select Customer" trong swimlane
    Page<CustomerRewardSummaryResponse> searchCustomers(String keyword, String sort, int page, int size);

    // Bước "Query loyalty_points... Calculate current balance"
    CustomerRewardSummaryResponse getCustomerRewardSummary(Integer customerId);

    // Bước "View reward points summary & transaction history" (+ optional filter)
    Page<LoyaltyPointResponse> getTransactionHistory(Integer customerId, CustomerRewardFilterRequest filter, int page, int size);

    // Ghi system_logs — bước cuối swimlane
    void logViewAccess(Integer managerId, Integer targetCustomerId);
}
