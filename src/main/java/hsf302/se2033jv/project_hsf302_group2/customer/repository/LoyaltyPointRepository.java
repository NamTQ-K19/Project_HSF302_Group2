package hsf302.se2033jv.project_hsf302_group2.customer.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.LoyaltyPoint;
import hsf302.se2033jv.project_hsf302_group2.common.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Integer> {

    @Query("SELECT COALESCE(SUM(lp.points), 0) FROM LoyaltyPoint lp WHERE lp.customer.userId = :customerId")
    Integer getTotalPointsByCustomerId(@Param("customerId") Integer customerId);
    // Derived query: lấy bản ghi mới nhất (sắp xếp giảm dần), dùng Pageable để giới hạn 1
    List<LoyaltyPoint> findTopByCustomerUserIdOrderByCreatedAtDescPointIdDesc(Integer customerId, Pageable pageable);

    List<LoyaltyPoint> findByCustomer_UserIdOrderByCreatedAtDesc(Integer userId);
    // Lấy lịch sử (trả về Page để hỗ trợ phân trang)
    Page<LoyaltyPoint> findByCustomerUserIdOrderByCreatedAtDesc(Integer customerId, Pageable pageable);

    List<LoyaltyPoint> findByCustomer_UserIdAndTransactionType(Integer userId, TransactionType transactionType);
    // Query động cho bộ lọc (type, date range) — trả về Page
    @Query("SELECT lp FROM LoyaltyPoint lp " +
           "WHERE lp.customer.userId = :customerId " +
           "AND (:transactionType IS NULL OR lp.transactionType = :transactionType) " +
           "AND (:fromDate IS NULL OR CAST(lp.createdAt AS java.time.LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(lp.createdAt AS java.time.LocalDate) <= :toDate) " +
           "ORDER BY lp.createdAt DESC")
    Page<LoyaltyPoint> findByCustomerUserIdWithFilter(
            @Param("customerId") Integer customerId,
            @Param("transactionType") TransactionType transactionType,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}

