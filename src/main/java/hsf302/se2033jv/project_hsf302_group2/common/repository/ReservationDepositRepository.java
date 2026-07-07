package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.ReservationDeposit;
import hsf302.se2033jv.project_hsf302_group2.common.enums.DepositPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationDepositRepository extends JpaRepository<ReservationDeposit, Integer> {

    Optional<ReservationDeposit> findByReservation_ReservationId(Integer reservationId);

    Optional<ReservationDeposit> findByReservationReservationId(Integer reservationId);

    List<ReservationDeposit> findByPaymentStatus(DepositPaymentStatus status);

    boolean existsByReservationReservationIdAndPaymentStatus(Integer reservationId, DepositPaymentStatus status);

    Optional<ReservationDeposit> findByTransactionRef(String transactionRef);
}
