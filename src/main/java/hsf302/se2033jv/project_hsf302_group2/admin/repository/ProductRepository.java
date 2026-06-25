package hsf302.se2033jv.project_hsf302_group2.admin.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isAvailable = true")
    List<Product> findActiveProducts();

    // Product variants with low stock (if stock tracking is needed)
    // For now, we'll use a placeholder for inventory alerts
}