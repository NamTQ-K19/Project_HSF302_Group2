// customer/repository/PaymentRepository.java
package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Payment;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByOrder_OrderId(Integer orderId);

    Optional<Payment> findByTransactionRef(String transactionRef);

    // Lấy danh sách đơn đã thanh toán thành công để Cashier in hóa đơn, mới nhất trước
    List<Payment> findByPaymentStatusOrderByPaidAtDesc(PaymentStatus paymentStatus);
}