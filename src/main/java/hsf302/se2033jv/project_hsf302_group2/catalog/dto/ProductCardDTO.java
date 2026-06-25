package hsf302.se2033jv.project_hsf302_group2.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for displaying a product card on Homepage / Search Results.
 * SRP: carries only what the card UI needs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardDTO {
    private Integer productId;
    private String name;
    private String categoryName;
    private String primaryImageUrl;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double averageRating;
    private Long reviewCount;
}
