package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isAvailable = true")
    List<Product> findActiveProducts();

    // Product variants with low stock (if stock tracking is needed)
    // For now, we'll use a placeholder for inventory alerts

    /**
     * Find all active, available products — used on Homepage.
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isAvailable = true")
    List<Product> findAllActiveAvailable();

    /**
     * Search products by name or description (case-insensitive).
     * Used for UC_02 Search Product.
     */
    @Query("""
        SELECT p FROM Product p
        WHERE p.isActive = true
          AND p.isAvailable = true
          AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find one active product by ID for the detail page (UC_03).
     */
    @Query("SELECT p FROM Product p WHERE p.productId = :id AND p.isActive = true")
    Optional<Product> findActiveById(@Param("id") Integer id);

    /**
     * Best-sellers: products with the most completed order details.
     * Used on Homepage (Top 6).
     */
    @Query("""
        SELECT p FROM Product p
        JOIN p.orderDetails od
        JOIN od.order o
        WHERE p.isActive = true AND p.isAvailable = true
          AND o.orderStatus = 'COMPLETED'
        GROUP BY p
        ORDER BY SUM(od.quantity) DESC
        """)
    List<Product> findBestSellers();

    /**
     * Find products by category ID.
     */
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :catId AND p.isActive = true AND p.isAvailable = true")
    List<Product> findByCategoryId(@Param("catId") Integer catId);
}