// customer/repository/LoyaltyPointRepository.java
package hsf302.se2033jv.project_hsf302_group2.customer.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.LoyaltyPoint;
import hsf302.se2033jv.project_hsf302_group2.common.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Integer> {

    @Query("SELECT COALESCE(SUM(lp.points), 0) FROM LoyaltyPoint lp WHERE lp.customer.userId = :customerId")
    Integer getTotalPointsByCustomerId(@Param("customerId") Long customerId);

    List<LoyaltyPoint> findByCustomer_UserIdOrderByCreatedAtDesc(Long userId);

    List<LoyaltyPoint> findByCustomer_UserIdAndTransactionType(Long userId, TransactionType transactionType);
}