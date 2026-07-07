package hsf302.se2033jv.project_hsf302_group2.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralConfigRequest {

    @NotBlank(message = "Tên cửa hàng không được để trống")
    @Size(max = 100, message = "Tên cửa hàng không được vượt quá 100 ký tự")
    private String siteName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String sitePhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String siteEmail;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String siteAddress;

    @NotBlank(message = "Giờ làm việc không được để trống")
    @Size(max = 50, message = "Giờ làm việc không được vượt quá 50 ký tự")
    private String siteHours;

    private String siteLogo;

    private String siteFavicon;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String siteDescription;
}