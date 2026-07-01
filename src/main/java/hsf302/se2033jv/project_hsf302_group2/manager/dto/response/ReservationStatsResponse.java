package hsf302.se2033jv.project_hsf302_group2.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatsResponse {

    private long totalReservations;
    private long pendingCount;
    private long confirmedCount;
    private long arrivedCount;
    private long completedCount;
    private long cancelledCount;
    private long noShowCount;
    private long paidDepositCount;
    private long totalWithDeposit;
    private double depositPaymentRate;  // % đã thanh toán cọc
}

