package hsf302.se2033jv.project_hsf302_group2.common.util;

import hsf302.se2033jv.project_hsf302_group2.common.cache.OtpData;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpUtil {

    private static final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final int OTP_EXPIRE_MINUTES = 5; // 5 phút cho BrewMaster

    /**
     * Tạo mã OTP ngẫu nhiên 6 chữ số
     */
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Lưu OTP vào cache với thời gian hết hạn
     */
    public void saveOtp(String email, String otp) {
        OtpData otpData = OtpData.builder()
                .otp(otp)
                .expireTime(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES))
                .build();
        otpStorage.put(email, otpData);
    }

    /**
     * Lưu OTP kèm dữ liệu bổ sung
     */
    public void saveOtpWithData(String email, String otp, Object data) {
        OtpData otpData = OtpData.builder()
                .otp(otp)
                .expireTime(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES))
                .data(data)
                .build();
        otpStorage.put(email, otpData);
    }

    /**
     * Xác thực OTP
     * @return true nếu OTP hợp lệ, false nếu không
     */
    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);

        if (otpData == null) {
            return false;
        }

        // Kiểm tra hết hạn
        if (LocalDateTime.now().isAfter(otpData.getExpireTime())) {
            otpStorage.remove(email);
            return false;
        }

        return otpData.getOtp().equals(otp);
    }

    /**
     * Lấy dữ liệu kèm theo OTP (nếu có)
     */
    public Object getOtpData(String email) {
        OtpData otpData = otpStorage.get(email);
        if (otpData == null) {
            return null;
        }
        return otpData.getData();
    }

    /**
     * Xóa OTP khỏi cache
     */
    public void removeOtp(String email) {
        otpStorage.remove(email);
    }

    /**
     * Kiểm tra OTP có tồn tại không
     */
    public boolean hasOtp(String email) {
        return otpStorage.containsKey(email);
    }

    /**
     * Lấy thời gian hết hạn của OTP
     */
    public LocalDateTime getOtpExpireTime(String email) {
        OtpData otpData = otpStorage.get(email);
        if (otpData == null) {
            return null;
        }
        return otpData.getExpireTime();
    }

    /**
     * Làm sạch OTP đã hết hạn (có thể gọi định kỳ)
     */
    public void cleanExpiredOtps() {
        otpStorage.entrySet().removeIf(entry ->
                LocalDateTime.now().isAfter(entry.getValue().getExpireTime())
        );
    }
}