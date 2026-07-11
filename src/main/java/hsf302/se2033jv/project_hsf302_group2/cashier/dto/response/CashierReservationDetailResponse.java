package hsf302.se2033jv.project_hsf302_group2.cashier.dto.response;

import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CashierReservationDetailResponse {
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
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String cancelledByName;
    private LocalDateTime createdAt;
    
    // Table Details
    private List<Integer> tableIds;
    private String tablesDisplay;

    // Deposit details
    private Integer depositId;
    private BigDecimal depositAmount;
    private String depositPaymentStatus;
    private String depositPaymentStatusDisplay;
    private String transactionRef;
    private String paymentMethodName;
    
    // Refund details
    private BigDecimal refundAmount;
    private String refundStatus;
    private String refundStatusDisplay;
    private String refundNote;
}
