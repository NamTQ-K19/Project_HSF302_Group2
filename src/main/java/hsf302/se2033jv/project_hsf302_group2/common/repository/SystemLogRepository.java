package hsf302.se2033jv.project_hsf302_group2.admin.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog,Integer> {
    @Query("SELECT l FROM SystemLog l ORDER BY l.createdAt DESC")
    List<SystemLog> findTop5ByOrderByCreatedAtDesc();
}
