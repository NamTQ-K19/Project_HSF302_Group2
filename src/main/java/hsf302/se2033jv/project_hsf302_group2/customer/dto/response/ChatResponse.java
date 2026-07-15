package hsf302.se2033jv.project_hsf302_group2.customer.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private boolean success;
    private String reply;       // câu trả lời AI
    private String errorMessage; // chỉ có giá trị khi success = false
}
