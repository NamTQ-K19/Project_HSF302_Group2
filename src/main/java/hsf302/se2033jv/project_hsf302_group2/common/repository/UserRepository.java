package hsf302.se2033jv.project_hsf302_group2.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:role IS NULL OR :role = '' OR LOWER(u.role.roleName) = LOWER(:role)) " +
            "AND (:status IS NULL OR :status = '' OR " +
            "(:status = 'ACTIVE' AND u.status = true) OR " +
            "(:status = 'LOCKED' AND u.status = false))")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role") String role,
                           @Param("status") String status,
                           Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR u.phone LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = true AND u.role.roleName IN ('MANAGER', 'CASHIER', 'BARISTA')")
    Long countActiveStaff();

    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = false")
    long countLockedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT u.role.roleName as roleName, COUNT(u) as count FROM User u GROUP BY u.role.roleName")
    List<Map<String, Object>> countUsersByRole();
}
