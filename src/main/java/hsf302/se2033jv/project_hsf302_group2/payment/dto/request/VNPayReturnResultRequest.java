package hsf302.se2033jv.project_hsf302_group2.payment.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayReturnResultRequest {
    private boolean valid;             // Checksum hợp lệ?
    private boolean success;           // vnp_ResponseCode == "00"
    private Integer orderId;
    private String vnpTxnRef;
    private String vnpTransactionNo;
    private String message;
}