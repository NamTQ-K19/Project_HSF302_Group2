package hsf302.se2033jv.project_hsf302_group2.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Vui lòng nhập tin nhắn")
    @Size(max = 500, message = "Tin nhắn không được vượt quá 500 ký tự")
    private String message;
}