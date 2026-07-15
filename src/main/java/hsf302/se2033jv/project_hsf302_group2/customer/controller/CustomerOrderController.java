// customer/controller/CustomerOrderController.java
package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CustomerAddress;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.PaymentMethod;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Policy;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyActionType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyType;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PolicyRepository;
import hsf302.se2033jv.project_hsf302_group2.common.util.SecurityUtils;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.PlaceOrderRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderConfirmationResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderResponse;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CustomerAddressRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PaymentMethodRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerOrderService;
import hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/customer/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController {

    private final CustomerOrderService orderService;
    private final CustomerAddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;
    private final PolicyRepository policyRepository;

    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();

        List<CustomerAddress> addresses = addressRepository.findByCustomer_UserId(userId);
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();

        // Đọc tỷ lệ quy đổi điểm động từ policies (REDEEM + DISCOUNT)
        BigDecimal pointToVndRate = policyRepository
                .findByPolicyTypeAndActionType(PolicyType.REDEEM, PolicyActionType.DISCOUNT)
                .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                .map(Policy::getCurrencyValue)
                .orElse(BigDecimal.ZERO);

        // Đọc tỷ lệ tích điểm động từ policies (EARN + ORDER), đơn vị %
        BigDecimal earnPercent = policyRepository
                .findByPolicyTypeAndActionType(PolicyType.EARN, PolicyActionType.ORDER)
                .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                .map(Policy::getCurrencyValue)
                .orElse(BigDecimal.ZERO);

        model.addAttribute("addresses", addresses);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("pageTitle", "Thanh toán");
        model.addAttribute("pointToVndRate", pointToVndRate);
        model.addAttribute("earnPercent", earnPercent);
        return "customer/order/checkout";
    }

    @PostMapping("/place")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpRequest) {   // ← THÊM param này
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Customer {} placing online order", userId);
            OrderConfirmationResponse orderResponse = orderService.placeOnlineOrder(userId, request);

            // Nếu KHÔNG phải Tiền mặt → build URL redirect ra VNPay
            if (!"Tiền mặt".equalsIgnoreCase(orderResponse.getPaymentMethodName().trim())) {
                Order order = orderRepository.findById(orderResponse.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
                String paymentUrl = vnPayService.createPaymentUrl(order, httpRequest);
                orderResponse.setPaymentUrl(paymentUrl);
            }

            response.put("success", true);
            response.put("data", orderResponse);
            response.put("message", "Đặt hàng thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error placing order: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/confirmation/{orderId}")
    public String showConfirmation(@PathVariable Integer orderId, Model model) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        OrderResponse order = orderService.getOrderDetails(orderId, userId);
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Xác nhận đơn hàng");
        return "customer/order/confirmation";
    }



    @PutMapping("/cancel/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Integer orderId,
            @RequestParam(required = false) String reason) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Customer {} cancelling order {}", userId, orderId);

            OrderResponse order = orderService.cancelOrder(orderId, userId, reason);

            response.put("success", true);
            response.put("data", order);
            response.put("message", "Hủy đơn hàng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}