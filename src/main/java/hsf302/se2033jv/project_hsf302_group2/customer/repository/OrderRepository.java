// customer/repository/OrderRepository.java
package hsf302.se2033jv.project_hsf302_group2.customer.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByUser_UserIdAndOrderStatus(Long userId, OrderStatus status);
}