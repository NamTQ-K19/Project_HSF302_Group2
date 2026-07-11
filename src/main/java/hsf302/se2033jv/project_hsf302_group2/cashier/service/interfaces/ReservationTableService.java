package hsf302.se2033jv.project_hsf302_group2.cashier.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.cashier.dto.request.CashierReservationFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.request.CashierRefundRequest;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierReservationListResponse;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierReservationDetailResponse;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierRefundResponse;
import org.springframework.data.domain.Page;

public interface ReservationTableService {
    
    Page<CashierReservationListResponse> getReservations(CashierReservationFilterRequest filter, int page, int size);

    CashierReservationDetailResponse getReservationDetail(Integer reservationId);

    CashierRefundResponse processRefund(CashierRefundRequest request, String cashierUsername, String ipAddress);
}
