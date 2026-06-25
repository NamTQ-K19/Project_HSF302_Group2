// customer/repository/UserRepository.java
package hsf302.se2033jv.project_hsf302_group2.customer.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}