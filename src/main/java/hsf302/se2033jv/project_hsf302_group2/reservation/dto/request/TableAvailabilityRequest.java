package hsf302.se2033jv.project_hsf302_group2.reservation.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableAvailabilityRequest {

    @NotNull(message = "Ngày đặt bàn không được để trống")
    @Future(message = "Ngày đặt bàn phải là ngày trong tương lai")
    private LocalDate reservationDate;

    @NotNull(message = "Giờ đặt bàn không được để trống")
    private LocalTime reservationTime;

    @NotNull(message = "Số lượng khách không được để trống")
    @Min(value = 1, message = "Số lượng khách phải lớn hơn 0")
    private Integer partySize;
}
