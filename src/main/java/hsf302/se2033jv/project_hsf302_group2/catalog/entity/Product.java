package hsf302.se2033jv.project_hsf302_group2.catalog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import hsf302.se2033jv.project_hsf302_group2.ordering.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.customer.entity.Review;

/**
 * Entity class ánh xạ tới bảng 'products' trong database.
 * Đại diện cho một sản phẩm trong hệ thống.
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews;

    /**
     * Lifecycle callback - set createdAt và updatedAt khi insert
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isAvailable == null) {
            isAvailable = true;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    /**
     * Lifecycle callback - update updatedAt khi update
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

