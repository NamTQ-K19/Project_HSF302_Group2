// customer/repository/ProductVariantRepository.java
package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    List<ProductVariant> findByProduct_ProductId(Integer productId);

    Optional<ProductVariant> findByVariantIdAndIsAvailableTrue(Integer variantId);
}