package hsf302.se2033jv.project_hsf302_group2.manager.dto.response;

import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailResponse {

    private Integer reservationId;

    // Khách hàng
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String username;

    // Đặt bàn
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer durationMinutes;
    private Integer partySize;
    private String note;

    // Trạng thái & Tiền cọc
    private ReservationStatus status;
    private String statusLabel;
    private String depositStatus;
    private BigDecimal depositAmount;

    // Bàn (Danh sách các bàn và sức chứa)
    private List<TableInfo> assignedTables;

    // Lịch sử & Hủy
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cancelledByName;
    private String cancelledByRole; 
    private String cancellationReason;
    private LocalDateTime cancelledAt;

    // Order phát sinh
    private Integer orderId;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TableInfo {
        private Integer tableId;
        private Integer capacity;
    }
}
