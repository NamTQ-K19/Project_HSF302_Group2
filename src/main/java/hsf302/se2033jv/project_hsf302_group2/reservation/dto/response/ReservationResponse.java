package hsf302.se2033jv.project_hsf302_group2.reservation.dto.response;

import hsf302.se2033jv.project_hsf302_group2.common.enums.DepositPaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class ReservationResponse {

    private Integer reservationId;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer partySize;
    private Integer durationMinutes;

    private ReservationStatus status;       // enum trực tiếp
    private String statusVi;               // "Chờ xác nhận", "Đã xác nhận", ...

    private String note;

    private BigDecimal depositAmount;       // null nếu không có deposit
    private DepositPaymentStatus depositStatus; // enum trực tiếp, null nếu không có
    private String depositStatusVi;        // null nếu không có

    private boolean canCancel;             // true nếu PENDING hoặc CONFIRMED
    private List<Integer> tableIds;        // danh sách table_id

    private LocalDateTime createdAt;
}
