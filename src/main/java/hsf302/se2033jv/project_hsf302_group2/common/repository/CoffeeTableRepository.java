package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CoffeeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CoffeeTableRepository extends JpaRepository<CoffeeTable, Integer> {

    // Lấy tất cả bàn đang hoạt động
    @Query("SELECT t FROM CoffeeTable t WHERE t.isActive = true")
    List<CoffeeTable> findAllActiveTables();

    // Lấy bàn đang hoạt động và có sức chứa >= capacity
    @Query("SELECT t FROM CoffeeTable t WHERE t.isActive = true AND t.capacity >= :capacity")
    List<CoffeeTable> findActiveTablesByCapacity(@Param("capacity") Integer capacity);

    // Kiểm tra một bàn cụ thể có trống không
    @Query(value = "SELECT COUNT(*) FROM tables t " +
            "WHERE t.table_id = :tableId " +
            "AND t.is_active = 1 " +
            "AND t.table_id NOT IN (" +
            "   SELECT rt.table_id FROM reservations r " +
            "   JOIN reservation_tables rt ON r.reservation_id = rt.reservation_id " +
            "   WHERE r.reservation_date = :date " +
            "   AND r.status IN ('PENDING', 'CONFIRMED') " +
            "   AND (" +
            "       r.reservation_time <= :time " +
            "       AND DATEADD(MINUTE, ISNULL(r.duration_minutes, 120), r.reservation_time) > :time" +
            "   )" +
            ") " +
            "AND t.table_id NOT IN (" +
            "   SELECT o.table_id FROM orders o " +
            "   WHERE o.table_id IS NOT NULL " +
            "   AND o.order_status IN ('PENDING', 'CONFIRMED', 'PREPARING')" +
            ")", nativeQuery = true)
    int countAvailableTableById(
            @Param("tableId") Integer tableId,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );
}