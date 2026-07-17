package hsf302.se2033jv.project_hsf302_group2.barista.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.barista.dto.BaristaOrderDTO;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;

import java.util.List;

public interface BaristaService {
    List<BaristaOrderDTO> getDashboardOrders();
    void updateItemStatus(Integer itemId, String status, String cancelReason);
    List<OrderDetail> getBaristaStats();
}
