package hsf302.se2033jv.project_hsf302_group2.manager.dto.response;

import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationScheduleResponse {

    private Integer reservationId;
    private String customerName;
    private String customerPhone;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer durationMinutes;
    private Integer partySize;
    private String tableNumbers;  // VD: "T01, T02"
    private ReservationStatus status;
    private String statusLabel;   // Tiếng Việt: "Chờ xác nhận", "Đã xác nhận", ...
    private String depositStatus; // "Đã cọc", "Chưa cọc", "Không có cọc"
    private BigDecimal depositAmount;
    private String note;
}

