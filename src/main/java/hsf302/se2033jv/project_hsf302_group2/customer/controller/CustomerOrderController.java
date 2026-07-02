// customer/controller/CustomerOrderController.java
package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CustomerAddress;
import hsf302.se2033jv.project_hsf302_group2.common.entity.PaymentMethod;
import hsf302.se2033jv.project_hsf302_group2.common.util.SecurityUtils;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.PlaceOrderRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderConfirmationResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.OrderResponse;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CustomerAddressRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PaymentMethodRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();

        List<CustomerAddress> addresses = addressRepository.findByCustomer_UserId(userId);
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();

        model.addAttribute("addresses", addresses);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("pageTitle", "Thanh toán");
        return "customer/order/checkout";
    }

    @PostMapping("/place")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody PlaceOrderRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Customer {} placing online order", userId);
            OrderConfirmationResponse orderResponse = orderService.placeOnlineOrder(userId, request);

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