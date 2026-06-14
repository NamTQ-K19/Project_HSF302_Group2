package hsf302.se2033jv.project_hsf302_group2.ordering.entity;

import hsf302.se2033jv.project_hsf302_group2.auth.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderType;
import hsf302.se2033jv.project_hsf302_group2.customer.entity.CustomerAddress;
import hsf302.se2033jv.project_hsf302_group2.reservation.entity.CoffeeTable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import hsf302.se2033jv.project_hsf302_group2.customer.entity.Review;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ tới bảng 'orders'
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id")
    private CoffeeTable table;

    @Column(name = "order_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(name = "order_status", length = 15)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_address_id")
    private CustomerAddress deliveryAddress;

    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "created_at", columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (shippingFee == null) shippingFee = BigDecimal.ZERO;
        if (pointsEarned == null) pointsEarned = 0;
        if (orderStatus == null) orderStatus = OrderStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

