package hsf302.se2033jv.project_hsf302_group2.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationConfigRequest {

    @NotNull(message = "Số tiền đặt cọc không được để trống")
    @Min(value = 0, message = "Số tiền đặt cọc không được âm")
    private Long depositAmount;

    @NotNull(message = "Thời gian giữ bàn không được để trống")
    @Min(value = 1, message = "Thời gian giữ bàn phải lớn hơn 0")
    private Integer holdMinutes;

    @NotNull(message = "Số lượng đặt bàn tối đa mỗi ngày không được để trống")
    @Min(value = 1, message = "Số lượng đặt bàn tối đa mỗi ngày phải lớn hơn 0")
    private Integer maxPerDay;

    @NotNull(message = "Số ngày đặt trước tối đa không được để trống")
    @Min(value = 1, message = "Số ngày đặt trước tối đa phải lớn hơn 0")
    private Integer maxAdvanceDays;

    @NotNull(message = "Số giờ đặt trước tối thiểu không được để trống")
    @Min(value = 0, message = "Số giờ đặt trước tối thiểu không được âm")
    private Integer minAdvanceHours;

    @NotNull(message = "Số lượng khách tối đa mỗi bàn không được để trống")
    @Min(value = 1, message = "Số lượng khách tối đa mỗi bàn phải lớn hơn 0")
    private Integer maxPartySize;

    @NotNull(message = "Thời gian hủy trước không được để trống")
    @Min(value = 0, message = "Thời gian hủy trước không được âm")
    private Integer cancelBeforeMinutes;
}
