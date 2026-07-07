package hsf302.se2033jv.project_hsf302_group2.reservation.dto.response;

import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeReservationResponse {
    private Integer reservationId;
    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer partySize;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer durationMinutes;
    private ReservationStatus status;
    private String statusDisplay;
    private String note;
    private List<Integer> tableIds;
    private List<String> tableNumbers;
    private BigDecimal depositAmount;
    private String depositStatus;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
    private String formattedReservationDateTime;
}
