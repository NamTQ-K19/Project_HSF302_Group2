package hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.payment.dto.request.VNPayReturnResultRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

public interface VNPayService {

    String createPaymentUrl(Order order, HttpServletRequest request);

    VNPayReturnResultRequest processReturn(Map<String, String> params);

    String createPaymentUrlForReservation(Integer reservationId, BigDecimal amount, HttpServletRequest request);

    VNPayReturnResultRequest processReturnForReservation(Map<String, String> params);
}