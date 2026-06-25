package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ReservationScheduleFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationScheduleResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationStatsResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ReservationScheduleService {

    Page<ReservationScheduleResponse> getSchedule(ReservationScheduleFilterRequest filter, int page, int size);

    ReservationScheduleResponse getDetail(Integer reservationId);

    void cancelReservation(Integer reservationId, String reason, Integer cancelledByUserId);

    ReservationStatsResponse getStats(LocalDate fromDate, LocalDate toDate);

    ReservationStatsResponse getStatsAll();
}

