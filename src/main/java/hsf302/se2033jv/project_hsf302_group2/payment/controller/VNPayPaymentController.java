package hsf302.se2033jv.project_hsf302_group2.payment.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Payment;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PaymentRepository;
import hsf302.se2033jv.project_hsf302_group2.common.util.SecurityUtils;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerOrderService;
import hsf302.se2033jv.project_hsf302_group2.payment.dto.request.VNPayReturnResultRequest;
import hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/customer/payment/vnpay")
@RequiredArgsConstructor
public class VNPayPaymentController {

    private final VNPayService vnPayService;
    private final CustomerOrderService customerOrderService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /**
     * VNPay redirect trình duyệt khách về đây sau khi thanh toán (thành công hoặc thất bại)
     */
    @GetMapping("/return")
    public String vnpayReturn(@RequestParam Map<String, String> allParams) {
        VNPayReturnResultRequest result = vnPayService.processReturn(allParams);
        log.info("VNPay return: orderId={}, valid={}, success={}, message={}",
                result.getOrderId(), result.isValid(), result.isSuccess(), result.getMessage());

        if (result.getOrderId() == null) {
            return "redirect:/customer/orders/history?paymentError=true";
        }

        customerOrderService.handleGatewayPaymentResult(
                result.getOrderId(),
                result.isValid() && result.isSuccess(),
                result.getVnpTransactionNo(),
                allParams.toString()
        );

        if (result.isValid() && result.isSuccess()) {
            return "redirect:/customer/orders/confirmation/" + result.getOrderId();
        }
        return "redirect:/customer/orders/confirmation/" + result.getOrderId() + "?paymentFailed=true";
    }

    @GetMapping("/retry/{orderId}")
    public String retryPayment(@PathVariable Integer orderId,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();

        Order order;
        try {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        } catch (ResourceNotFoundException e) {
            log.warn("Retry payment failed - order not found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/orders/history";
        }

        // ← THÊM: Kiểm tra quyền sở hữu TRƯỚC TIÊN — nếu vi phạm, redirect thẳng sang /403 có sẵn.
        // KHÔNG redirect về confirmation vì trang đó cũng tự chặn quyền xem, sẽ gây lỗi 500 chồng lỗi.
        if (!order.getUser().getUserId().equals(userId)) {
            log.warn("User {} attempted to retry payment for order {} owned by another user", userId, orderId);
            return "redirect:/403";
        }

        try {
            Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thanh toán"));

            boolean isCash = "Tiền mặt".equalsIgnoreCase(payment.getPaymentMethod().getName().trim());
            boolean paymentRetryable = payment.getPaymentStatus() == PaymentStatus.PENDING
                    || payment.getPaymentStatus() == PaymentStatus.FAILED;
            boolean orderRetryable = order.getOrderStatus() == OrderStatus.PENDING
                    || order.getOrderStatus() == OrderStatus.CONFIRMED;

            if (isCash) {
                throw new BusinessException("Đơn hàng thanh toán bằng Tiền mặt không thể thanh toán lại qua cổng thanh toán online");
            }
            if (!paymentRetryable || !orderRetryable) {
                throw new BusinessException("Đơn hàng này không thể thanh toán lại ở trạng thái hiện tại");
            }

            String paymentUrl = vnPayService.createPaymentUrl(order, request);
            return "redirect:" + paymentUrl;

        } catch (ResourceNotFoundException e) {
            log.warn("Retry payment failed - not found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/orders/history";
        } catch (BusinessException e) {
            log.warn("Retry payment failed for order {}: {}", orderId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/orders/confirmation/" + orderId;
        }
    }
}