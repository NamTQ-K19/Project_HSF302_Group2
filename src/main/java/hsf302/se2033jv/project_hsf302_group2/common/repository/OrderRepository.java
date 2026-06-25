package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND o.createdAt >= :startDate")
    Long countCompletedOrdersSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND o.createdAt >= :startDate")
    BigDecimal sumCompletedOrderAmountsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND o.createdAt BETWEEN :startDate AND :endDate")
    Long countCompletedOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedOrderAmountsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT TOP 5 p.name as itemName, COUNT(od.product_id) as count " +
            "FROM order_details od " +
            "JOIN products p ON od.product_id = p.product_id " +
            "JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.created_at >= :startDate AND o.order_status = 'COMPLETED' " +
            "GROUP BY p.product_id, p.name " +
            "ORDER BY count DESC", nativeQuery = true)
    List<Object[]> findTopSellingItemsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'COMPLETED' OR o.orderStatus = 'PREPARING' OR o.orderStatus = 'READY' ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders();

    @Query("SELECT o FROM Order o WHERE o.orderStatus IN ('COMPLETED', 'PREPARING', 'READY', 'PENDING') ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersWithStatus();

    @Query(value = "SELECT " +
            "FORMAT(o.created_at, 'yyyy-MM-dd') as date, " +
            "COALESCE(SUM(o.total_amount), 0) as total_sales, " +
            "COUNT(o.order_id) as order_count " +
            "FROM orders o " +
            "WHERE o.order_status = 'COMPLETED' " +
            "AND o.created_at >= :startDate " +
            "GROUP BY FORMAT(o.created_at, 'yyyy-MM-dd') " +
            "ORDER BY date", nativeQuery = true)
    List<Object[]> getDailySalesStats(@Param("startDate") LocalDateTime startDate);
}