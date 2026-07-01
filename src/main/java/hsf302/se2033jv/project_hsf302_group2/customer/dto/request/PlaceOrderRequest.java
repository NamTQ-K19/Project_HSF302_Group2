// customer/dto/request/PlaceOrderRequest.java
package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {
    private Integer deliveryAddressId;
    private Integer paymentMethodId;
    private String note;
    private Boolean usePoints = false;
    private List<Integer> itemIds;
}