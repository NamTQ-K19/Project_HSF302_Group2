package hsf302.se2033jv.project_hsf302_group2.cashier.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CashierRefundResponse {
    private boolean success;
    private String message;
    private String transactionRef;
}
