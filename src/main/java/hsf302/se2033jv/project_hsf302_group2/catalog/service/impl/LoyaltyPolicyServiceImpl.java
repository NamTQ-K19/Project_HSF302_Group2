package hsf302.se2033jv.project_hsf302_group2.catalog.service.impl;
import hsf302.se2033jv.project_hsf302_group2.catalog.dto.PolicyDTO;
import hsf302.se2033jv.project_hsf302_group2.catalog.repository.PolicyRepository;
import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.ILoyaltyPolicyService;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UC_04 – Loyalty Reward Policy service implementation.
 * SRP: retrieves and filters policy records only.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoyaltyPolicyServiceImpl implements ILoyaltyPolicyService {

    private final PolicyRepository policyRepository;
    private final ProductMapper productMapper;

    @Override
    public List<PolicyDTO> getActivePolicies() {
        return policyRepository.findAllActive()
                .stream()
                .map(productMapper::toPolicyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyDTO> getEarnPolicies() {
        return policyRepository.findAllActive()
                .stream()
                .filter(p -> PolicyType.EARN.equals(p.getPolicyType()))
                .map(productMapper::toPolicyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyDTO> getRedeemPolicies() {
        return policyRepository.findAllActive()
                .stream()
                .filter(p -> PolicyType.REDEEM.equals(p.getPolicyType()))
                .map(productMapper::toPolicyDTO)
                .collect(Collectors.toList());
    }
}
