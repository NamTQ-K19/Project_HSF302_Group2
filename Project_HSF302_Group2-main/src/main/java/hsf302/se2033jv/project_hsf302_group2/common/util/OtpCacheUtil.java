package hsf302.se2033jv.project_hsf302_group2.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpCacheUtil {

    // ── Inner class đại diện 1 OTP entry ──────────────────────
    private static class OtpEntry {
        private String otp;
        private LocalDateTime expiryTime;
        boolean used;

        public OtpEntry(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.used = false;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }


    private final ConcurrentHashMap<String, OtpEntry> otpCache = new ConcurrentHashMap<>();
    private SecureRandom secureRandom = new SecureRandom();

    private String buildKey(Integer userId, String email){
        return userId + ":" + email.toLowerCase();
    }

    public String generateAndStore(Integer userId, String email, int expiryMinutes){
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(expiryMinutes);

        otpCache.put(buildKey(userId, email), new OtpEntry(otp, expiryTime));
        return otp;
    }

    public boolean verifyOtp(Integer userId, String email, String otp){
        String key = buildKey(userId, email);
        OtpEntry entry = otpCache.get(key);

        if (entry == null || entry.used) {
            return false;
        }

        if (entry.isExpired()) {
            otpCache.remove(key);
            return false;
        }

        if (!entry.otp.equals(otp)) {
            return false;
        }

        entry.used = true;
        otpCache.remove(buildKey(userId, email));
        return true;
    }

    public void invalidate(Integer userId, String email){
        otpCache.remove(buildKey(userId, email));
    }
}
