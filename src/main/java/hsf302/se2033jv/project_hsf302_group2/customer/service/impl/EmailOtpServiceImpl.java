package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.util.OtpCacheUtil;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.EmailOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailOtpServiceImpl implements EmailOtpService {

    private final JavaMailSender mailSender;
    private final OtpCacheUtil otpCacheUtil;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Override
    @Async("emailExecutor")
    public void generateAndSendOtp(User user, String newEmail) {
        String otp = otpCacheUtil.generateAndStore(user.getUserId(), newEmail, otpExpiryMinutes);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(newEmail);
        message.setSubject("Mã OTP xác minh email mới – Cafe Shop");
        message.setText(
                "Xin chào " + user.getFirstName() + ",\n\n"
                        + "Mã OTP để xác minh email mới của bạn là: " + otp + "\n\n"
                        + "Mã có hiệu lực trong " + otpExpiryMinutes + " phút.\n\n"
                        + "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n"
                        + "Trân trọng,\nHệ thống Quản lý Cafe Shop"
        );
        mailSender.send(message);
    }

    @Override
    public boolean verifyOtp(User user, String newEmail, String otpCode) {
        return otpCacheUtil.verifyOtp(user.getUserId(), newEmail, otpCode);
    }
}
