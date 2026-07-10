package hsf302.se2033jv.project_hsf302_group2.reservation.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Reservation;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.util.SecurityUtils;
import hsf302.se2033jv.project_hsf302_group2.payment.dto.request.VNPayReturnResultRequest;
import hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces.VNPayService;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.response.ReservationDepositInfo;
import hsf302.se2033jv.project_hsf302_group2.reservation.service.interfaces.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/customer/reservations/payment/vnpay")
@RequiredArgsConstructor
@Slf4j
public class ReservationVNPayReturnController {

    private final VNPayService vnPayService;
    private final ReservationService reservationService;

    /**
     * VNPay redirect trình duyệt khách về đây sau khi thanh toán
     */
    @GetMapping("/return")
    public String vnpayReturn(@RequestParam Map<String, String> allParams) {
        VNPayReturnResultRequest result = vnPayService.processReturnForReservation(allParams);
        log.info("VNPay return for reservation: id={}, valid={}, success={}, message={}",
                result.getOrderId(), result.isValid(), result.isSuccess(), result.getMessage());

        Integer reservationId = result.getOrderId();

        if (reservationId == null) {
            return "redirect:/customer/reservations?paymentError=true";
        }

        // GỌI SERVICE XỬ LÝ KẾT QUẢ THANH TOÁN
        reservationService.handleGatewayPaymentResult(
                reservationId,
                result.isValid() && result.isSuccess(),
                result.getVnpTransactionNo(),
                allParams.toString()
        );

        if (result.isValid() && result.isSuccess()) {
            return "redirect:/customer/reservations/" + reservationId + "/confirmation";
        }
        return "redirect:/customer/reservations/" + reservationId + "/confirmation?paymentFailed=true";
    }

    /**
     * Retry payment cho Reservation
     */
    @GetMapping("/retry/{reservationId}")
    public String retryPayment(@PathVariable Integer reservationId,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();

        // LẤY RESERVATION QUA SERVICE
        Reservation reservation;
        try {
            reservation = reservationService.getReservationById(reservationId);
        } catch (ResourceNotFoundException e) {
            log.warn("Retry payment failed - reservation not found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/reservations";
        }

        // KIỂM TRA QUYỀN SỞ HỮU
        if (!reservation.getCustomer().getUserId().equals(userId)) {
            log.warn("User {} attempted to retry payment for reservation {} owned by another user", userId, reservationId);
            return "redirect:/403";
        }

        try {
            // KIỂM TRA TRẠNG THÁI RESERVATION
            if (reservation.getStatus() != ReservationStatus.PENDING) {
                throw new BusinessException("Đặt bàn không ở trạng thái chờ thanh toán");
            }

            // LẤY DEPOSIT INFO QUA SERVICE (KHÔNG GỌI REPO TRỰC TIẾP)
            ReservationDepositInfo depositInfo = reservationService.getDepositByReservationId(reservationId);

            if (depositInfo == null) {
                throw new BusinessException("Không tìm thấy thông tin tiền cọc");
            }

            // KIỂM TRA TRẠNG THÁI DEPOSIT
            if (depositInfo.isPaid()) {
                throw new BusinessException("Tiền cọc đã được thanh toán");
            }

            if (depositInfo.isRefunded()) {
                throw new BusinessException("Tiền cọc đã được hoàn trả, không thể thanh toán lại");
            }

            // LẤY SỐ TIỀN CỌC VÀ TẠO URL THANH TOÁN
            BigDecimal depositAmount = depositInfo.getDepositAmount();
            String paymentUrl = vnPayService.createPaymentUrlForReservation(reservationId, depositAmount, request);

            log.info("Retry payment for reservation {}: redirecting to VNPay", reservationId);
            return "redirect:" + paymentUrl;

        } catch (BusinessException e) {
            log.warn("Retry payment failed for reservation {}: {}", reservationId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/reservations/" + reservationId + "/deposit";
        } catch (Exception e) {
            log.error("Error retrying payment for reservation {}: {}", reservationId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/customer/reservations/" + reservationId + "/deposit";
        }
    }
}