package hsf302.se2033jv.project_hsf302_group2.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Vui lòng chọn danh mục cho sản phẩm")
    private Integer categoryId;

    private java.util.List<VariantRequest> variants;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantRequest {
        private Integer id; // Dùng khi cập nhật
        private String variantName;
        private hsf302.se2033jv.project_hsf302_group2.common.enums.VariantSize size;
        private hsf302.se2033jv.project_hsf302_group2.common.enums.VariantTemperature temperature;
        private java.math.BigDecimal price;
        private org.springframework.web.multipart.MultipartFile image;
    }
}
