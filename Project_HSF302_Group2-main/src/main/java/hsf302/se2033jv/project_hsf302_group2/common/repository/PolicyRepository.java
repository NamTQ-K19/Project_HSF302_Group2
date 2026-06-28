package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for loyalty reward policies used in UC_04.
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Integer> {

    /**
     * Return all active policies ordered by type (EARN first, then REDEEM).
     */
    @Query("SELECT p FROM Policy p WHERE p.status = true ORDER BY p.policyType DESC, p.policyName")
    List<Policy> findAllActive();
}
