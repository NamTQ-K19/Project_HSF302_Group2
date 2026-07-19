package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.LoyaltyPoint;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReferenceType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.TransactionType;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.LoyaltyPointResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.LoyaltyPointFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.common.repository.LoyaltyPointRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyPointServiceImpl implements LoyaltyPointService {

    private final LoyaltyPointRepository loyaltyPointRepository;

    @Override
    public int getCurrentBalance(Integer customerId) {
        Pageable pageable = PageRequest.of(0, 1);
        List<LoyaltyPoint> result = loyaltyPointRepository.findTopByCustomerUserIdOrderByCreatedAtDescPointIdDesc(customerId, pageable);
        return (result == null || result.isEmpty()) ? 0 : result.get(0).getBalanceAfter();
    }

    @Override
    public Page<LoyaltyPointResponse> getHistory(Integer customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return loyaltyPointRepository
                .findByCustomerUserIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Page<LoyaltyPointResponse> getHistoryWithFilter(Integer customerId, LoyaltyPointFilterRequest filter, int page, int size) {
        TransactionType typeEnum = null;
        String typeString = filter.getTransactionType();
        if (typeString != null && !typeString.isEmpty()) {
            try {
                typeEnum = TransactionType.valueOf(typeString.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        return loyaltyPointRepository
                .findByCustomerUserIdWithFilter(customerId, typeEnum, filter.getFromDate(), filter.getToDate(), pageable)
                .map(this::mapToDTO);
    }

    private LoyaltyPointResponse mapToDTO(LoyaltyPoint lp) {
        LoyaltyPointResponse dto = new LoyaltyPointResponse();
        dto.setPointId(lp.getPointId());
        dto.setTransactionType(lp.getTransactionType() != null ? lp.getTransactionType() : null);
        dto.setPoints(lp.getPoints());
        dto.setBalanceAfter(lp.getBalanceAfter());
        dto.setReferenceType(lp.getReferenceType() != null ? lp.getReferenceType() : null);
        dto.setReferenceId(lp.getReferenceId());
        dto.setNote(lp.getNote());
        dto.setCreatedAt(lp.getCreatedAt());
        return dto;
    }

    @Override
    public void creditOrderPoints(Order order) {
        Integer pointsEarned = order.getPointsEarned();
        if (pointsEarned == null || pointsEarned <= 0) return;

        boolean alreadyCredited = loyaltyPointRepository.existsByReferenceTypeAndReferenceIdAndTransactionType(
                ReferenceType.ORDER, order.getOrderId(), TransactionType.EARN);
        if (alreadyCredited) return;

        int newBalance = getCurrentBalance(order.getUser().getUserId()) + pointsEarned;

        LoyaltyPoint earn = LoyaltyPoint.builder()
                .customer(order.getUser())
                .transactionType(TransactionType.EARN)
                .points(pointsEarned)
                .balanceAfter(newBalance)
                .referenceType(ReferenceType.ORDER)
                .referenceId(order.getOrderId())
                .note("Tích điểm từ đơn hàng #" + order.getOrderId())
                .createdAt(java.time.LocalDateTime.now())
                .build();
        loyaltyPointRepository.save(earn);
    }

}

