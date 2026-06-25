package hsf302.se2033jv.project_hsf302_group2.reservation.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByCustomer_UserIdOrderByCreatedAtDesc(Integer customerId);

    Optional<Reservation> findByReservationIdAndCustomer_UserId(Integer reservationId, Integer customerId);
}

