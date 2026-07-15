package hsf302.se2033jv.project_hsf302_group2.common.ai;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTurnDTO implements Serializable {
    private String sender;   // "user" hoặc "ai"
    private String content;  // với sender=ai: đã là HTML an toàn (có <strong>, <a>, <br>...)
}
