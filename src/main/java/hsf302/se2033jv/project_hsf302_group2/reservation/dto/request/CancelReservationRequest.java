package hsf302.se2033jv.project_hsf302_group2.reservation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelReservationRequest {

    private Integer reservationId; // truyền qua hidden input

    @Size(max = 500, message = "Lý do hủy không được vượt quá 500 ký tự")
    private String cancellationReason; // không bắt buộc
}
