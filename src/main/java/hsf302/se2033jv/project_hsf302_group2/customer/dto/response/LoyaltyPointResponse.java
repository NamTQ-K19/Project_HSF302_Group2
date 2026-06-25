package hsf302.se2033jv.project_hsf302_group2.customer.dto.response;

import hsf302.se2033jv.project_hsf302_group2.common.enums.ReferenceType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoyaltyPointResponse {

    @NotNull(message = "Mã điểm tích lũy không được để trống")
    private Integer pointId;

    @NotNull(message = "Loại giao dịch không được để trống")
    private TransactionType transactionType;        // 'EARN' | 'REDEEM''

    @NotNull(message = "Số điểm không được để trống")
    private Integer points;                // dương = cộng, âm = trừ

    @NotNull(message = "Số dư sau giao dịch không được để trống")
    private Integer balanceAfter;          // số dư sau giao dịch

    private ReferenceType referenceType;          // 'ORDER' | 'REVIEW'

    private Integer referenceId;


    private String note;

    @NotNull(message =  "Thời gian tạo không được để trống")
    private LocalDateTime createdAt;
}

