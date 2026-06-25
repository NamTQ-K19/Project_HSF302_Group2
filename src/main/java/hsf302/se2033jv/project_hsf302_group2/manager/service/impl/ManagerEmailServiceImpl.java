package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ManagerEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ManagerEmailServiceImpl implements ManagerEmailService {

    private final JavaMailSender mailSender;

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
}

