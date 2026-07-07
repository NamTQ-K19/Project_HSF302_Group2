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
public class ReservationConfirmationResponse {

    private String message;
    private Boolean success;
    private MakeReservationResponse reservation;
    private BigDecimal depositAmount;
    private String paymentUrl;
    private Integer holdMinutes;
    private String redirectUrl;
}
