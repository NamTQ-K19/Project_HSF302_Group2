package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Policy;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyActionType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // lọc theo policy_type / action_type / status — tham số nào null thì bỏ qua điều kiện đó
    @Query("SELECT p FROM Policy p WHERE " +
            "(:policyType IS NULL OR p.policyType = :policyType) AND " +
            "(:actionType IS NULL OR p.actionType = :actionType) AND " +
            "(:status IS NULL OR p.status = :status) " +
            "ORDER BY p.policyType DESC, p.policyName")
    List<Policy> findByFilters(@Param("policyType") PolicyType policyType,
                               @Param("actionType") PolicyActionType actionType,
                               @Param("status") Boolean status);

    Optional<Policy> findByPolicyTypeAndActionType(PolicyType policyType, PolicyActionType actionType);

}
