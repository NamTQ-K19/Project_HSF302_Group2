package hsf302.se2033jv.project_hsf302_group2.common.entity;

import hsf302.se2033jv.project_hsf302_group2.common.enums.DepositPaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ tới bảng 'reservation_deposits'
 */
@Entity
@Table(name = "reservation_deposits",
       uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposit_id")
    private Integer depositId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "deposit_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal depositAmount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_status", length = 15)
    @Enumerated(EnumType.STRING)
    private DepositPaymentStatus paymentStatus;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_status", length = 10)
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    @Column(name = "refund_note", columnDefinition = "NVARCHAR(MAX)")
    private String refundNote;

    @Column(name = "applied_to_order")
    private Boolean appliedToOrder;

    @Column(name = "created_at", columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (refundAmount == null) refundAmount = BigDecimal.ZERO;
        if (appliedToOrder == null) appliedToOrder = false;
        if (paymentStatus == null) paymentStatus = DepositPaymentStatus.PENDING;
        if (refundStatus == null) refundStatus = RefundStatus.NONE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

