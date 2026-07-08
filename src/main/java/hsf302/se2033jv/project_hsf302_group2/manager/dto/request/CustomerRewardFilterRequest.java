package hsf302.se2033jv.project_hsf302_group2.manager.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRewardFilterRequest {
    private String keyword;          // tìm theo tên / email / username / phone
    private String transactionType;  // null | 'EARN' | 'REDEEM' | 'ADJUST'
    private LocalDate fromDate;
    private LocalDate toDate;
}
