package hsf302.se2033jv.project_hsf302_group2.catalog.entity;

import hsf302.se2033jv.project_hsf302_group2.common.enums.VariantSize;
import hsf302.se2033jv.project_hsf302_group2.common.enums.VariantTemperature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import hsf302.se2033jv.project_hsf302_group2.ordering.entity.OrderDetail;

/**
 * Entity class ánh xạ tới bảng 'product_variants' trong database.
 * Đại diện cho một variant (kích thước, nhiệt độ) của sản phẩm.
 */
@Entity
@Table(name = "product_variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Integer variantId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "variant_name", length = 100)
    private String variantName;

    @Column(name = "size", length = 5)
    @Enumerated(EnumType.STRING)
    private VariantSize size;

    @Column(name = "temperature", length = 10)
    @Enumerated(EnumType.STRING)
    private VariantTemperature temperature;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductImage> images;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    /**
     * Lifecycle callback - set default values khi insert
     */
    @PrePersist
    protected void onCreate() {
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        if (isAvailable == null) {
            isAvailable = true;
        }
    }
}

