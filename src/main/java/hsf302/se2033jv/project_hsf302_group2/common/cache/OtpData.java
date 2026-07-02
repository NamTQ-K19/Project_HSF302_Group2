package hsf302.se2033jv.project_hsf302_group2.common.cache;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpData {
    private String otp;
    private LocalDateTime expireTime;
    private Object data;
}