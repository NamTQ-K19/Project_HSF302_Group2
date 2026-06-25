// customer/dto/request/CartUpdateRequest.java
package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import lombok.Data;

@Data
public class CartUpdateRequest {
    private Integer cartItemId;
    private String action; // increase, decrease
}