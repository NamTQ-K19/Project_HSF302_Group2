package hsf302.se2033jv.project_hsf302_group2.ordering.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPosResponse {
    private Integer productId;
    private String name;
    private Integer categoryId;
    private String categoryName;
    private String imageUrl;
    private String description;
    private List<VariantPosResponse> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantPosResponse {
        private Integer variantId;
        private String variantName;
        private String size;
        private String temperature;
        private BigDecimal price;
        private Boolean isAvailable;
    }
}
