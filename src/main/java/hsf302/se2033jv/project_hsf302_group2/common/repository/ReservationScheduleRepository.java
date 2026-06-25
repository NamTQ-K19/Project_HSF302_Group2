package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Reservation;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationScheduleRepository extends JpaRepository<Reservation, Integer> {

    // Get all reservations with pagination
    Page<Reservation> findAllByOrderByReservationDateDescReservationTimeAsc(Pageable pageable);

    // Filter by date range
    Page<Reservation> findByReservationDateBetweenOrderByReservationTimeAsc(
            LocalDate fromDate, LocalDate toDate, Pageable pageable);

    // Filter by date range and status
    Page<Reservation> findByReservationDateBetweenAndStatusOrderByReservationTimeAsc(
            LocalDate fromDate, LocalDate toDate, ReservationStatus status, Pageable pageable);

    // Get all by date range (non-paginated)
    List<Reservation> findByReservationDateBetweenOrderByReservationTimeAsc(LocalDate fromDate, LocalDate toDate);

    // Find by reservation ID and status
    Optional<Reservation> findByReservationIdAndStatusIn(Integer id, List<ReservationStatus> statuses);

    // Count all reservations
    long count();

    // Count by status (all dates)
    long countByStatus(ReservationStatus status);

    // Count by date range
    long countByReservationDateBetween(LocalDate fromDate, LocalDate toDate);

    // Count by date range and status
    long countByReservationDateBetweenAndStatus(LocalDate fromDate, LocalDate toDate, ReservationStatus status);
}


