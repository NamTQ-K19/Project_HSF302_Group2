package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.LoyaltyPointResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.LoyaltyPointFilterRequest;
import org.springframework.data.domain.Page;

public interface LoyaltyPointService {

    int getCurrentBalance(Integer customerId);

    Page<LoyaltyPointResponse> getHistory(Integer customerId, int page, int size);

    Page<LoyaltyPointResponse> getHistoryWithFilter(Integer customerId, LoyaltyPointFilterRequest filter, int page, int size);

    /**
     * Cộng điểm tích lũy cho 1 đơn hàng — CHỈ được gọi khi Order.status chuyển sang COMPLETED.
     * Có kiểm tra idempotent (không cộng 2 lần cho cùng 1 order).
     */
    void creditOrderPoints(Order order);
}

