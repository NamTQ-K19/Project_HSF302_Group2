// D:\SU26\HSF302\Practice\Project_HSF302_Group2\src\main\java\hsf302\se2033jv\project_hsf302_group2\admin\repository\UserRepository.java

package hsf302.se2033jv.project_hsf302_group2.admin.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);

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
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
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