// customer/repository/OrderDetailRepository.java
package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    List<OrderDetail> findByOrder_OrderId(Integer orderId);

    @Query("SELECT od FROM OrderDetail od WHERE od.itemStatus = 'COMPLETED' OR od.itemStatus = 'CANCELLED'")
    List<OrderDetail> findBaristaStats();
}