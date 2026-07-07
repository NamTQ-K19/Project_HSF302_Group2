package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog,Integer> {
    @Query("SELECT l FROM SystemLog l ORDER BY l.createdAt DESC")
    List<SystemLog> findTop5ByOrderByCreatedAtDesc();

    Page<SystemLog> findByTargetTypeContainingIgnoreCaseOrActionContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCreatedAtBetween(
            String targetType, String action, String description,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<SystemLog> findByTargetTypeContainingIgnoreCaseOrActionContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndActionAndCreatedAtBetween(
            String targetType, String action1, String description,
            String action2, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<SystemLog> findByActionAndCreatedAtBetween(
            String action, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<SystemLog> findByCreatedAtBetween(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
