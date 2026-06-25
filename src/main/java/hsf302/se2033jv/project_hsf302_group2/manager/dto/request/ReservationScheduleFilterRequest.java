package hsf302.se2033jv.project_hsf302_group2.manager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationScheduleFilterRequest {

    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;  // nullable, enum value
}

