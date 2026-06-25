package hsf302.se2033jv.project_hsf302_group2.common.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpData {
    private String otp;
    private LocalDateTime expireTime;
    private Object data; // Lưu thêm dữ liệu kèm theo nếu cần
}