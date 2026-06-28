package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordVerifyRequest {

    @NotBlank(message = "Mật khẩu không được để trống")
    private String currentPassword;
}
