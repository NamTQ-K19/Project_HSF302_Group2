package hsf302.se2033jv.project_hsf302_group2.reservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDepositInfo {
    private Integer depositId;
    private BigDecimal depositAmount;
    private String paymentStatus;
    private boolean paid;
    private boolean refunded;
    private String transactionRef;
}