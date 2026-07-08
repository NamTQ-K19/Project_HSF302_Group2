package hsf302.se2033jv.project_hsf302_group2.payment.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Payment;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PaymentRepository;
import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.EmailService;
import hsf302.se2033jv.project_hsf302_group2.payment.dto.response.InvoiceListItemResponse;
import hsf302.se2033jv.project_hsf302_group2.payment.dto.response.InvoiceResponse;
import hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static hsf302.se2033jv.project_hsf302_group2.common.enums.OrderType.COUNTER;
import static hsf302.se2033jv.project_hsf302_group2.common.enums.OrderType.ONLINE;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Override
    public List<InvoiceListItemResponse> getInvoiceList() {
        List<Payment> payments = paymentRepository.findByPaymentStatusOrderByPaidAtDesc(PaymentStatus.SUCCESS);

        return payments.stream()
                .map(this::mapToListItem)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceResponse getInvoice(Integer orderId) {
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng chưa có thông tin thanh toán"));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessException("CANNOT_PRINT_INVOICE");
        }

        return mapToInvoiceResponse(payment);
    }

    @Override
    public void resendInvoiceEmail(Integer orderId) {
        InvoiceResponse invoice = getInvoice(orderId); // tái sử dụng validate + mapping

        if (invoice.getCustomerEmail() == null || invoice.getCustomerEmail().isBlank()) {
            log.warn("Đơn hàng {} không có email khách hàng, bỏ qua gửi email", orderId);
            return;
        }

        String itemsSummary = invoice.getItems().stream()
                .map(i -> "- " + i.getProductName()
                        + (i.getVariantName() != null ? " (" + i.getVariantName() + ")" : "")
                        + " x" + i.getQuantity() + ": " + i.getItemTotal() + " đ")
                .collect(Collectors.joining("\n"));

        emailService.sendInvoiceEmail(
                invoice.getCustomerEmail(),
                invoice.getCustomerName(),
                invoice.getOrderId(),
                itemsSummary,
                invoice.getTotalAmount()
        );
    }

    private InvoiceListItemResponse mapToListItem(Payment payment) {
        Order order = payment.getOrder();
        return InvoiceListItemResponse.builder()
                .orderId(order.getOrderId())
                .orderTypeLabel(getOrderTypeLabel(order))
                .customerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .tableNumber(order.getTable() != null ? "T" + String.format("%02d", order.getTable().getTableId()) : null)
                .totalAmount(order.getTotalAmount())
                .paymentMethodName(payment.getPaymentMethod().getName())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private InvoiceResponse mapToInvoiceResponse(Payment payment) {
        Order order = payment.getOrder();

        List<InvoiceResponse.InvoiceItemResponse> items = order.getOrderDetails().stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());

        boolean isCash = "cash".equalsIgnoreCase(payment.getPaymentMethod().getName());

        return InvoiceResponse.builder()
                .orderId(order.getOrderId())
                .orderTypeLabel(getOrderTypeLabel(order))
                .tableNumber(order.getTable() != null ? "T" + String.format("%02d", order.getTable().getTableId()) : null)
                .orderDate(order.getCreatedAt())
                .customerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .customerEmail(order.getUser().getEmail())
                .items(items)
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .pointsEarned(order.getPointsEarned())
                .paymentMethodName(payment.getPaymentMethod().getName())
                .paymentStatusLabel("Đã thanh toán")
                .transactionRef(isCash ? null : payment.getTransactionRef())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private InvoiceResponse.InvoiceItemResponse mapToItemResponse(OrderDetail detail) {
        return InvoiceResponse.InvoiceItemResponse.builder()
                .productName(detail.getProductNameSnapshot())
                .variantName(detail.getVariantNameSnapshot())
                .unitPrice(detail.getPriceSnapshot())
                .quantity(detail.getQuantity())
                .itemTotal(detail.getItemTotal())
                .specialNote(detail.getSpecialNote())
                .build();
    }

    private String getOrderTypeLabel(Order order) {
        return switch (order.getOrderType()) {
            case COUNTER -> "Tại quầy";
            case ONLINE -> "Online";
        };
    }
}
