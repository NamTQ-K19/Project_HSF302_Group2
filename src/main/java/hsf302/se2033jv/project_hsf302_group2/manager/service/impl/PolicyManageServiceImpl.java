package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Policy;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyActionType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyType;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PolicyRepository;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.PolicyRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.PolicyResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.PolicyManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyActionType.DISCOUNT;
import static hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyActionType.REVIEW;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyManageServiceImpl implements PolicyManageService {

    private final PolicyRepository policyRepository;

    // ── Nhãn hiển thị tiếng Việt ─────────────────────────────
    private String policyTypeLabel(PolicyType type) {
        return type == PolicyType.EARN ? "Tích điểm" : "Đổi điểm";
    }

    private String actionTypeLabel(PolicyActionType type) {
        return switch (type) {
            case DISCOUNT -> "Giảm giá";
            case ORDER -> "Đơn hàng";
            case REVIEW -> "Đánh giá";
        };
    }

    private PolicyResponse toResponse(Policy p) {
        return PolicyResponse.builder()
                .policyId(p.getPolicyId())
                .policyName(p.getPolicyName())
                .policyType(p.getPolicyType().name())
                .policyTypeLabel(policyTypeLabel(p.getPolicyType()))
                .actionType(p.getActionType().name())
                .actionTypeLabel(actionTypeLabel(p.getActionType()))
                .currencyValue(p.getCurrencyValue())
                .unit(p.getUnit())
                .comment(p.getComment())
                .status(p.getStatus() != null ? p.getStatus() : true)
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyResponse> getPolicies(String policyType, String actionType, String status) {
        PolicyType pt = (policyType != null && !policyType.isBlank())
                ? PolicyType.fromValue(policyType) : null;
        PolicyActionType at = (actionType != null && !actionType.isBlank())
                ? PolicyActionType.fromValue(actionType) : null;
        Boolean st = (status != null && !status.isBlank())
                ? Boolean.valueOf(status) : null;

        return policyRepository.findByFilters(pt, at, st).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updatePolicy(Integer policyId, PolicyRequest request) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chính sách #" + policyId));

        policy.setPolicyName(request.getPolicyName().trim());
        policy.setCurrencyValue(request.getCurrencyValue());
        policy.setUnit(request.getUnit().trim());
        policy.setComment(request.getComment());
        policyRepository.save(policy);
    }

    @Override
    public void toggleStatus(Integer policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chính sách #" + policyId));

        // UPDATE policies SET status = ~status (flip BIT)
        policy.setStatus(!Boolean.TRUE.equals(policy.getStatus()));
        policy.setUpdatedAt(LocalDateTime.now());
        policyRepository.save(policy);
    }
}
