package hsf302.se2033jv.project_hsf302_group2.common.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.EmailService;
import hsf302.se2033jv.project_hsf302_group2.common.util.OtpCacheUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final OtpCacheUtil otpCacheUtil;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Override
    @Async("emailExecutor")
    public void sendEmail(String to, String subject, String body) {
        buildAndSendEmail(to, subject, body);
    }

    private void buildAndSendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("✅ Email sent to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email");
        }
    }

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
    @Async("emailExecutor")
    public void sendReservationCancellationEmail(String toEmail, String customerName,
                                                 LocalDate date, LocalTime time, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Café Shop] Thông báo hủy đặt bàn");
        message.setText(
                "Xin chào " + customerName + ",\n\n"
                        + "Đặt bàn của bạn vào ngày " + date + " lúc " + time + " đã được hủy.\n\n"
                        + "Lý do: " + reason + "\n\n"
                        + "Nếu có thắc mắc, vui lòng liên hệ quán.\n\n"
                        + "Trân trọng,\nHệ thống Quản lý Café Shop"
        );
        mailSender.send(message);
    }

    @Override
    public void sendInvoiceEmail(String toEmail, String customerName, Integer orderId,
                                 String itemsSummary, BigDecimal totalAmount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Café Shop] Hóa đơn đơn hàng #" + orderId);
        message.setText(
                "Xin chào " + customerName + ",\n\n"
                        + "Cảm ơn bạn đã sử dụng dịch vụ tại Café Shop. Dưới đây là hóa đơn cho đơn hàng #" + orderId + ":\n\n"
                        + itemsSummary + "\n\n"
                        + "Tổng cộng: " + totalAmount + " đ\n\n"
                        + "Trân trọng,\nHệ thống Quản lý Café Shop"
        );
        mailSender.send(message);
    }
}
