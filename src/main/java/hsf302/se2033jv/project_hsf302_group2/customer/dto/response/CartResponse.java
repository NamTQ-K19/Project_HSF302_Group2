// customer/dto/response/CartResponse.java
package hsf302.se2033jv.project_hsf302_group2.customer.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {
    private Integer cartId;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal total;
    private Integer loyaltyPoints;
    private List<CartItemResponse> items;

    @Data
    public static class CartItemResponse {
        private Integer cartItemId;
        private Integer productId;
        private Integer variantId;
        private String productName;
        private String variantName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal itemTotal;
        private String specialNote;
        private Boolean selected = true;
    }
}