package hsf302.se2033jv.project_hsf302_group2.ordering.controller;

import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.ConfirmPaymentRequest;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.CreateOrderRequest;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.UpdateStatusRequest;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.response.*;
import hsf302.se2033jv.project_hsf302_group2.ordering.service.interfaces.OrderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order/api")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'MANAGER')")
public class OrderingApiController {

    private final OrderingService orderingService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductPosResponse>> getProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(orderingService.getPosProducts(categoryId, keyword));
    }

    @GetMapping("/tables")
    public ResponseEntity<List<TablePosResponse>> getTables() {
        return ResponseEntity.ok(orderingService.getPosTables());
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerPosResponse>> searchCustomers(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(orderingService.searchCustomers(keyword));
    }

    @PostMapping("/create")
    public ResponseEntity<OrderPosDetailResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication auth) {
        String username = auth != null ? auth.getName() : "cashier";
        return ResponseEntity.ok(orderingService.createCounterOrder(request, username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderPosDetailResponse> getOrderDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(orderingService.getOrderDetail(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderPosDetailResponse> updateStatus(
            @PathVariable Integer id,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(orderingService.updateOrderStatus(id, request));
    }

    @PutMapping("/{id}/confirm-payment")
    public ResponseEntity<OrderPosDetailResponse> confirmPayment(
            @PathVariable Integer id,
            @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(orderingService.confirmPayment(id, request));
    }
}
