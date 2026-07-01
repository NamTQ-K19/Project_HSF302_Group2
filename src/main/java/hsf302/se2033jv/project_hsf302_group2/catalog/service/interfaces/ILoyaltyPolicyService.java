package hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces;
import hsf302.se2033jv.project_hsf302_group2.catalog.dto.PolicyDTO;

import java.util.List;

/**
 * Service contract for UC_04 View Loyalty Reward Policy.
 * ISP: only policy retrieval — independent of product concerns.
 */
public interface ILoyaltyPolicyService {

    /**
     * Returns all active loyalty policies split by type for display.
     */
    List<PolicyDTO> getActivePolicies();

    /**
     * Returns only EARN-type policies.
     */
    List<PolicyDTO> getEarnPolicies();

    /**
     * Returns only REDEEM-type policies.
     */
    List<PolicyDTO> getRedeemPolicies();
}