package hsf302.se2033jv.project_hsf302_group2.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigRequest {

    private Boolean maintenanceMode;

    @Size(max = 500, message = "Thông báo bảo trì không được vượt quá 500 ký tự")
    private String maintenanceMessage;

    @NotNull(message = "Số ngày lưu log không được để trống")
    @Min(value = 1, message = "Số ngày lưu log phải lớn hơn 0")
    private Integer logRetentionDays;

    @NotBlank(message = "Ngôn ngữ mặc định không được để trống")
    @Size(max = 10, message = "Ngôn ngữ mặc định không được vượt quá 10 ký tự")
    private String defaultLanguage;

    @NotNull(message = "Số item mỗi trang không được để trống")
    @Min(value = 1, message = "Số item mỗi trang phải lớn hơn 0")
    private Integer itemsPerPage;
}