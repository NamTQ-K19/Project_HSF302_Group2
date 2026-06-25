package hsf302.se2033jv.project_hsf302_group2.customer.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Integer> {

    List<CustomerAddress> findByCustomerUserIdOrderByCreatedAtDesc(Integer userId);

    Optional<CustomerAddress> findByAddressIdAndCustomerUserId(Integer addressId, Integer userId);

    boolean existsByCustomer_UserIdAndRecipientPhone(Integer customerId, String recipientPhone);

    boolean existsByCustomer_UserIdAndRecipientPhoneAndAddressIdNot(Integer customerId, String recipientPhone, Integer addressId);
}
