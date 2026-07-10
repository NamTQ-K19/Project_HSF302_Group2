package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Reservation;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByCustomer_UserIdOrderByCreatedAtDesc(Integer customerId);

    Optional<Reservation> findByReservationIdAndCustomer_UserId(Integer reservationId, Integer customerId);

    Page<Reservation> findByCustomerUserId(Integer customerId, Pageable pageable);

    List<Reservation> findByCustomerUserIdAndStatus(Integer customerId, ReservationStatus status);

    Optional<Reservation> findByReservationIdAndCustomerUserId(Integer reservationId, Integer customerId);

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate = :date AND r.reservationTime = :time AND r.status IN ('PENDING', 'CONFIRMED')")
    List<Reservation> findActiveReservationsByDateTime(@Param("date") LocalDate date, @Param("time") LocalTime time);

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate = :date AND r.status IN ('PENDING', 'CONFIRMED')")
    List<Reservation> findActiveReservationsByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status AND r.createdAt < :time")
    Long countByStatusAndCreatedAtBefore(@Param("status") ReservationStatus status, @Param("time") LocalDateTime time);

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate = :date AND r.reservationTime BETWEEN :startTime AND :endTime")
    List<Reservation> findByReservationDateAndTimeRange(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime time);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.customer.userId = :customerId AND r.reservationDate = :date AND r.status IN ('PENDING', 'CONFIRMED')")
    int countReservationsByCustomerAndDate(@Param("customerId") Integer customerId, @Param("date") LocalDate date);

    @Query(value = "SELECT DISTINCT rt.table_id FROM reservations r " +
            "JOIN reservation_tables rt ON r.reservation_id = rt.reservation_id " +
            "WHERE r.reservation_date = :date " +
            "AND r.status IN ('PENDING', 'CONFIRMED') " +
            "AND (" +
            "   CAST(r.reservation_time AS DATETIME) <= CAST(:time AS DATETIME) " +
            "   AND DATEADD(MINUTE, COALESCE(r.duration_minutes, 120), CAST(r.reservation_time AS DATETIME)) > CAST(:time AS DATETIME)" +
            ")", nativeQuery = true)
    List<Integer> findReservedTableIds(@Param("date") LocalDate date, @Param("time") LocalTime time);

    Optional<Reservation> findByOrder_OrderId(Integer orderId);
}

