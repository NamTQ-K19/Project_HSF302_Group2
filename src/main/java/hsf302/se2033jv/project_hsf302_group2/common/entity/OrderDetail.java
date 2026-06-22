package hsf302.se2033jv.project_hsf302_group2.common.entity;

import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity ánh xạ tới bảng 'order_details'
 */
@Entity
@Table(name = "order_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "product_name_snapshot", length = 150, nullable = false)
    private String productNameSnapshot;

    @Column(name = "variant_name_snapshot", length = 100)
    private String variantNameSnapshot;

    @Column(name = "price_snapshot", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceSnapshot;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "item_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal itemTotal;

    @Column(name = "special_note", columnDefinition = "NVARCHAR(MAX)")
    private String specialNote;

    @Column(name = "item_status", length = 15)
    @Enumerated(EnumType.STRING)
    private OrderItemStatus itemStatus;

    @PrePersist
    protected void onCreate() {
        if (priceSnapshot == null) priceSnapshot = BigDecimal.ZERO;
        if (quantity == null || quantity <= 0) quantity = 1;
        if (itemTotal == null) itemTotal = priceSnapshot.multiply(BigDecimal.valueOf(quantity));
        if (itemStatus == null) itemStatus = OrderItemStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        if (itemTotal == null && priceSnapshot != null && quantity != null) {
            itemTotal = priceSnapshot.multiply(BigDecimal.valueOf(quantity));
        }
    }
}

