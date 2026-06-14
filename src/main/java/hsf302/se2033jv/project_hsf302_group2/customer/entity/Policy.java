package hsf302.se2033jv.project_hsf302_group2.customer.entity;

import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyActionType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ tới bảng 'policies'
 */
@Entity
@Table(name = "policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Integer policyId;

    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(name = "currency_value", precision = 10, scale = 2)
    private BigDecimal currencyValue;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "action_type", length = 15, nullable = false)
    @Enumerated(EnumType.STRING)
    private PolicyActionType actionType;

    @Column(name = "policy_type", length = 15, nullable = false)
    @Enumerated(EnumType.STRING)
    private PolicyType policyType;

    @Column(name = "policy_name", length = 150, nullable = false)
    private String policyName;

    @Column(name = "created_at", columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (currencyValue == null) currencyValue = BigDecimal.ZERO;
        if (status == null) status = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

