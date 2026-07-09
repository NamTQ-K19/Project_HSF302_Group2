package hsf302.se2033jv.project_hsf302_group2.ordering.dto.request;

import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest {
    private String keyword;
    private OrderStatus status;
    private OrderType type;
    private PaymentStatus paymentStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;
}
