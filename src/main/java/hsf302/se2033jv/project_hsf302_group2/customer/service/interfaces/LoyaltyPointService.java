package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.LoyaltyPointResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.LoyaltyPointFilterRequest;
import org.springframework.data.domain.Page;

public interface LoyaltyPointService {

    int getCurrentBalance(Integer customerId);

    Page<LoyaltyPointResponse> getHistory(Integer customerId, int page, int size);

    Page<LoyaltyPointResponse> getHistoryWithFilter(Integer customerId, LoyaltyPointFilterRequest filter, int page, int size);
}

