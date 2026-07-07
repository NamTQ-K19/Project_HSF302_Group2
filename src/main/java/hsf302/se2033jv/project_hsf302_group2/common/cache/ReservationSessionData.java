package hsf302.se2033jv.project_hsf302_group2.common.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSessionData {
    private Integer customerId;
    private String customerName;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer partySize;
    private Integer durationMinutes;
    private Integer selectedTableId;
    private String note;
    private BigDecimal depositAmount;
    private Integer holdMinutes;
}