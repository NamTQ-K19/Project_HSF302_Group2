package hsf302.se2033jv.project_hsf302_group2.cashier.dto.response;

import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class CashierReservationListResponse {
    private Integer reservationId;
    private String customerName;
    private String customerPhone;
    private Integer partySize;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer durationMinutes;
    private ReservationStatus status;
    private String statusDisplay;
    private BigDecimal depositAmount;
    private String depositPaymentStatus;
    private String depositPaymentStatusDisplay;
    private String paymentMethodName;
    private LocalDateTime createdAt;
}
