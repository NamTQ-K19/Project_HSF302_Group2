package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.PolicyRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.PolicyResponse;

import java.util.List;

public interface PolicyManageService {

    List<PolicyResponse> getPolicies(String policyType, String actionType, String status);

    void updatePolicy(Integer policyId, PolicyRequest request);

    void toggleStatus(Integer policyId);
}
