package hsf302.se2033jv.project_hsf302_group2.manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequest {

    @NotBlank(message = "Tên chính sách không được để trống")
    private String policyName;

    private String policyType;      // EARN | REDEEM

    private String actionType;      // DISCOUNT | ORDER | REVIEW

    @NotNull(message = "Giá trị không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị phải lớn hơn 0")
    private BigDecimal currencyValue;

    @NotBlank(message = "Đơn vị không được để trống")
    private String unit;

    private String comment;
}