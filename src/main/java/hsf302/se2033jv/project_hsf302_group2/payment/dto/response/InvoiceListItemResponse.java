package hsf302.se2033jv.project_hsf302_group2.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceListItemResponse {

    private Integer orderId;
    private String orderTypeLabel;   // "Tại quầy" / "Online"
    private String customerName;
    private String tableNumber;      // "T01", null nếu là online
    private BigDecimal totalAmount;
    private String paymentMethodName;
    private LocalDateTime paidAt;
}
