// customer/repository/CartItemRepository.java
package hsf302.se2033jv.project_hsf302_group2.customer.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByCart_CartId(Integer cartId);

    Optional<CartItem> findByCart_CartIdAndVariant_VariantId(Integer cartId, Integer variantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteByCart_CartId(@Param("cartId") Integer cartId);
}