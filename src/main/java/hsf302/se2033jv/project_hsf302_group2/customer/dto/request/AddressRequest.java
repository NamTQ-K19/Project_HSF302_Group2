package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @Size(max = 50, message = "Nhãn phải có ít hơn 50 ký tự.")
    private String label;

    @NotBlank(message = "Địa chỉ không được để trống.")
    @Size(max = 500, message = "Địa chỉ phải có ít hơn 500 ký tự.")
    private String fullAddress;

    @NotBlank(message = "Người nhận không được để trống.")
    @Size(max = 150, message = "Người nhận phải có ít hơn 150 ký tự.")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống.")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số.")
    private String recipientPhone;
}
