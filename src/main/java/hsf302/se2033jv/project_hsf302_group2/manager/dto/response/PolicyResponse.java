package hsf302.se2033jv.project_hsf302_group2.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Integer policyId;
    private String policyName;
    private String policyType;       // EARN | REDEEM
    private String policyTypeLabel;  // "Tích điểm" | "Đổi điểm"
    private String actionType;       // DISCOUNT | ORDER | REVIEW
    private String actionTypeLabel;  // "Giảm giá" | "Đơn hàng" | "Đánh giá"
    private BigDecimal currencyValue;
    private String unit;
    private String comment;
    private boolean status;
    private LocalDateTime updatedAt;
}