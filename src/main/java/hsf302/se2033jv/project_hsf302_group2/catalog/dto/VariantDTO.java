package hsf302.se2033jv.project_hsf302_group2.catalog.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for a single product variant (size + temperature + price).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantDTO {
    private Integer variantId;
    private String variantName;
    private String size;
    private String temperature;
    private BigDecimal price;
    private Boolean isAvailable;
}
