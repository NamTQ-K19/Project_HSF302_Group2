package hsf302.se2033jv.project_hsf302_group2.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalTime;

@Configuration
@Getter
public class ReservationConfig {

    @Value("${reservation.deposit.amount:50000}")
    private BigDecimal defaultDepositAmount;

    @Value("${reservation.hold.duration.minutes:10}")
    private Integer holdDurationMinutes;

    @Value("${reservation.max.party.size:10}")
    private Integer maxPartySize;

    @Value("${reservation.opening.time:07:00}")
    private String openingTime;

    @Value("${reservation.closing.time:22:00}")
    private String closingTime;

    @Value("${reservation.cancellation.ban.days:3}")
    private Integer cancellationBanDays;

    @Value("${reservation.advance.days:30}")
    private Integer maxAdvanceDays;

    public LocalTime getOpeningTime() {
        return LocalTime.parse(openingTime);
    }

    public LocalTime getClosingTime() {
        return LocalTime.parse(closingTime);
    }
}