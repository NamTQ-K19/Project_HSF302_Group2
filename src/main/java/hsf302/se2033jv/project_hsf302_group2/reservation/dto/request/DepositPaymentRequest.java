package hsf302.se2033jv.project_hsf302_group2.reservation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositPaymentRequest {

    @NotNull(message = "ID đặt bàn không được để trống")
    private Integer reservationId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private Integer paymentMethodId;

    @NotNull(message = "Số tiền cọc không được để trống")
    @Min(value = 1, message = "Số tiền cọc phải lớn hơn 0")
    private BigDecimal amount;

    private String transactionRef;
}
