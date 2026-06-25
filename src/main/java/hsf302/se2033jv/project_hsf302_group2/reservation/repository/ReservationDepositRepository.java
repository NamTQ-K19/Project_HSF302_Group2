package hsf302.se2033jv.project_hsf302_group2.reservation.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.ReservationDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationDepositRepository extends JpaRepository<ReservationDeposit, Integer> {

    Optional<ReservationDeposit> findByReservation_ReservationId(Integer reservationId);
}
