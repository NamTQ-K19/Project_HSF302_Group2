package hsf302.se2033jv.project_hsf302_group2.barista.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;

@Data
@NoArgsConstructor
public class BaristaItemDTO {
    private Integer itemId;
    private String productName;
    private String variantName;
    private Integer quantity;
    private String specialNote;
    private String itemStatus;

    public BaristaItemDTO(OrderDetail detail) {
        this.itemId = detail.getItemId();
        this.productName = detail.getProductNameSnapshot();
        this.variantName = detail.getVariantNameSnapshot();
        this.quantity = detail.getQuantity();
        this.specialNote = detail.getSpecialNote();
        this.itemStatus = detail.getItemStatus().name();
    }
}
