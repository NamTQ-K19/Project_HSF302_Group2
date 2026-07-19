package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog,Integer> {
    @Query("SELECT l FROM SystemLog l ORDER BY l.createdAt DESC")
    List<SystemLog> findTop5ByOrderByCreatedAtDesc();

    // ← THAY 4 method cũ bằng đúng 1 method này
    @Query("SELECT l FROM SystemLog l LEFT JOIN l.user u " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "       LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:action IS NULL OR :action = '' OR l.action = :action) " +
            "AND l.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY l.createdAt DESC")
    Page<SystemLog> findWithFilters(
            @Param("keyword") String keyword,
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
