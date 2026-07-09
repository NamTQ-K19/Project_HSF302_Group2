package hsf302.se2033jv.project_hsf302_group2.barista.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class BaristaOrderDTO {
    private Integer orderId;
    private Integer tableId;
    private String orderType;
    private long waitMinutes;
    private LocalDateTime createdAt;
    private List<BaristaItemDTO> items;

    public BaristaOrderDTO(Order order) {
        this.orderId = order.getOrderId();
        if (order.getTable() != null) {
            this.tableId = order.getTable().getTableId();
        }
        this.orderType = order.getOrderType().name();
        this.createdAt = order.getCreatedAt();
        this.waitMinutes = ChronoUnit.MINUTES.between(this.createdAt, LocalDateTime.now());
        
        // Filter out completed/cancelled items if we only want to show pending/preparing ones
        // Or show them all. For now, we show them all and the frontend handles rendering.
        this.items = order.getOrderDetails().stream()
                .map(BaristaItemDTO::new)
                .collect(Collectors.toList());
    }
}
