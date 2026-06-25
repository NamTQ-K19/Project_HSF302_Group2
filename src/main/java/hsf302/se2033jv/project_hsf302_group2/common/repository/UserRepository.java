package hsf302.se2033jv.project_hsf302_group2.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    User getUserByUserId(int userId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailAndUserIdNot(String email, Integer userId);

    boolean existsByPhoneAndUserIdNot(String phone, Integer userId);

    boolean existsByUsernameAndUserIdNot(String username, Integer userId);
}
