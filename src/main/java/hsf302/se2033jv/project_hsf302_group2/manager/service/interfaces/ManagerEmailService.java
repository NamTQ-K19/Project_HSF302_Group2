package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ManagerEmailService {

    void sendReservationCancellationEmail(String toEmail, String customerName,
                                          LocalDate date, LocalTime time, String reason);
}

