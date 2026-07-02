package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 100, message = "Họ phải có ít hơn 100 ký tự")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên phải có ít hơn 100 ký tự")
    private String lastName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$",
            message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số.")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Không đúng định dạng email")
    @Size(max = 150, message = "Email phải có ít hơn 150 ký tự.")
    private String email;

    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(max = 50, message = "Tên người dùng phải có ít hơn 50 ký tự.")
    private String username;
}
