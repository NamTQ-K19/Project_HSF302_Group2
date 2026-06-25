package hsf302.se2033jv.project_hsf302_group2.common.repository;


import hsf302.se2033jv.project_hsf302_group2.common.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for category data used in navigation and homepage filtering.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.name")
    List<Category> findAllActive();
}
