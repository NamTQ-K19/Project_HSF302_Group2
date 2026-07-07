package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {

    Optional<SystemConfig> findByConfigKey(String configKey);

    List<SystemConfig> findByIsActiveTrue();

    @Query("SELECT s FROM SystemConfig s WHERE s.isActive = true AND s.configGroup = :group")
    List<SystemConfig> findByConfigGroup(String group);
}