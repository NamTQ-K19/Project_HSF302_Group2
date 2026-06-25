// customer/dto/request/AddToCartRequest.java
package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Integer productId;
    private Integer variantId;
    private Integer quantity;
    private String specialNote;
}