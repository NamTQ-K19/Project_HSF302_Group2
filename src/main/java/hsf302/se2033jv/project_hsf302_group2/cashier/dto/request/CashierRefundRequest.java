package hsf302.se2033jv.project_hsf302_group2.cashier.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CashierRefundRequest {
    @NotNull(message = "Reservation ID không được trống")
    private Integer reservationId;

    @NotNull(message = "Số tiền hoàn không được trống")
    @DecimalMin(value = "0.01", message = "Số tiền hoàn phải lớn hơn 0")
    private BigDecimal amount;

    private String note;
}
