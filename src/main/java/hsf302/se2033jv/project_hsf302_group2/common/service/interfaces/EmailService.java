package hsf302.se2033jv.project_hsf302_group2.common.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void generateAndSendOtp (User user, String newEmail);

    void sendReservationCancellationEmail(String toEmail, String customerName,
                                          LocalDate date, LocalTime time, String reason);
}
