package hsf302.se2033jv.project_hsf302_group2.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for displaying a loyalty reward policy (UC_04).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDTO {
    private Integer policyId;
    private String policyName;
    private String policyType;    // EARN | REDEEM
    private String actionType;    // ORDER | REVIEW | DISCOUNT
    private BigDecimal currencyValue;
    private String unit;
    private String comment;
    private Boolean status;
}
