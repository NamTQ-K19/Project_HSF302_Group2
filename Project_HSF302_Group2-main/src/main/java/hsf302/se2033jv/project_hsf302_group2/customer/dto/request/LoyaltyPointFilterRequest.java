package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoyaltyPointFilterRequest {

    private String transactionType;  // null = tất cả | 'EARN' | 'REDEEM' | 'ADJUST'

    private LocalDate fromDate;      // nullable

    private LocalDate toDate;        // nullable
}

