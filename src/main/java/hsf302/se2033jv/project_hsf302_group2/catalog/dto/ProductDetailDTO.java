package hsf302.se2033jv.project_hsf302_group2.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for the Product Details screen (UC_03).
 * SRP: contains all data required by the product detail page only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    private Integer productId;
    private String name;
    private String description;
    private String categoryName;
    private String primaryImageUrl;
    private List<String> allImageUrls;
    private List<VariantDTO> variants;
    private Double averageRating;
    private Long reviewCount;
    private List<ReviewDTO> reviews;
}
