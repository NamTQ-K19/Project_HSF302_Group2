package hsf302.se2033jv.project_hsf302_group2.cashier.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class CashierReservationFilterRequest {
    private String keyword;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
}
