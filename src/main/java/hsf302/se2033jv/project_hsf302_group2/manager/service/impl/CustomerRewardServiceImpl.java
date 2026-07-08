package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.LoyaltyPoint;
import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemLog;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.TransactionType;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.LoyaltyPointRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.SystemLogRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.LoyaltyPointResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CustomerRewardFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.CustomerRewardSummaryResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.CustomerRewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerRewardServiceImpl implements CustomerRewardService {

    private final UserRepository userRepository;
    private final LoyaltyPointRepository loyaltyPointRepository;
    private final SystemLogRepository systemLogRepository;

    @Override
    public Page<CustomerRewardSummaryResponse> searchCustomers(String keyword, String sort, int page, int size) {

        // Lấy TOÀN BỘ khách hàng khớp từ khóa — không phân trang ở DB
        // vì cần sort theo điểm (giá trị tính toán, không phải cột DB)
        String trimmedKeyword = (keyword != null) ? keyword.trim() : null;

        List<User> allUsers = userRepository
                .searchUsers(trimmedKeyword, "CUSTOMER", "", Pageable.unpaged())
                .getContent();

        // Map sang DTO kèm tính điểm cho từng khách hàng
        List<CustomerRewardSummaryResponse> allSummaries = allUsers.stream()
                .map(this::mapUserToSummary)
                .collect(Collectors.toList());

        // Sắp xếp theo điểm hiện tại nếu có yêu cầu
        if ("points_asc".equals(sort)) {
            allSummaries.sort(Comparator.comparing(CustomerRewardSummaryResponse::getCurrentBalance));
        } else if ("points_desc".equals(sort)) {
            allSummaries.sort(Comparator.comparing(CustomerRewardSummaryResponse::getCurrentBalance).reversed());
        }
        // Không truyền sort hoặc giá trị khác → giữ nguyên thứ tự mặc định (theo user_id)

        // Tự phân trang thủ công trên danh sách đã sort
        int start = Math.min(page * size, allSummaries.size());
        int end = Math.min(start + size, allSummaries.size());
        List<CustomerRewardSummaryResponse> pageContent = allSummaries.subList(start, end);

        return new PageImpl<>(pageContent, PageRequest.of(page, size), allSummaries.size());
    }

    @Override
    public CustomerRewardSummaryResponse getCustomerRewardSummary(Integer customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng"));
        return mapUserToSummary(customer);
    }

    @Override
    public Page<LoyaltyPointResponse> getTransactionHistory(Integer customerId, CustomerRewardFilterRequest filter, int page, int size) {
        TransactionType typeEnum = null;
        if (filter.getTransactionType() != null && !filter.getTransactionType().isEmpty()) {
            try {
                typeEnum = TransactionType.valueOf(filter.getTransactionType().toUpperCase());
            } catch (IllegalArgumentException ignored) { }
        }
        Pageable pageable = PageRequest.of(page, size);
        return loyaltyPointRepository
                .findByCustomerUserIdWithFilter(customerId, typeEnum, filter.getFromDate(), filter.getToDate(), pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void logViewAccess(Integer managerId, Integer targetCustomerId) {
        User manager = userRepository.findById(managerId).orElse(null);
        SystemLog log = SystemLog.builder()
                .user(manager)
                .action("VIEW_CUSTOMER_REWARD_POINTS")
                .targetType("User")
                .targetId(targetCustomerId)
                .description("Manager xem thông tin điểm tích lũy của khách hàng #" + targetCustomerId)
                .build();
        systemLogRepository.save(log);
    }

    // ---- Helper mapping methods ----

    private CustomerRewardSummaryResponse mapUserToSummary(User customer) {
        Integer customerId = customer.getUserId();
        Pageable top1 = PageRequest.of(0, 1);
        var latest = loyaltyPointRepository
                .findTopByCustomerUserIdOrderByCreatedAtDescPointIdDesc(customerId, top1);
        int currentBalance = latest.isEmpty() ? 0 : latest.get(0).getBalanceAfter();
        int totalEarned = loyaltyPointRepository.sumEarnedByCustomerId(customerId);
        int totalRedeemed = loyaltyPointRepository.sumRedeemedByCustomerId(customerId);

        return CustomerRewardSummaryResponse.builder()
                .customerId(customerId)
                .fullName(customer.getFirstName() + " " + customer.getLastName())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .avatarUrl(customer.getAvatarUrl())
                .status(customer.getStatus())
                .currentBalance(currentBalance)
                .totalEarned(totalEarned)
                .totalRedeemed(totalRedeemed)
                .build();
    }

    private LoyaltyPointResponse mapToDTO(LoyaltyPoint lp) {
        LoyaltyPointResponse dto = new LoyaltyPointResponse();
        dto.setPointId(lp.getPointId());
        dto.setTransactionType(lp.getTransactionType());
        dto.setPoints(lp.getPoints());
        dto.setBalanceAfter(lp.getBalanceAfter());
        dto.setReferenceType(lp.getReferenceType());
        dto.setReferenceId(lp.getReferenceId());
        dto.setNote(lp.getNote());
        dto.setCreatedAt(lp.getCreatedAt());
        return dto;
    }
}
