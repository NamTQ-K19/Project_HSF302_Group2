package hsf302.se2033jv.project_hsf302_group2.payment.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cashier/invoices")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CASHIER')")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ProfileService profileService;

    @GetMapping
    public String showInvoiceList(Model model, Authentication auth) {
        User cashier = profileService.getCurrentUser(auth.getName());

        model.addAttribute("cashier", cashier);
        model.addAttribute("invoices", invoiceService.getInvoiceList());
        return "cashier/invoice/list";
    }

    @GetMapping("/{orderId}")
    public String showInvoiceDetail(@PathVariable Integer orderId, Model model, Authentication auth) {
        User cashier = profileService.getCurrentUser(auth.getName());

        model.addAttribute("cashier", cashier);
        model.addAttribute("invoice", invoiceService.getInvoice(orderId));
        return "cashier/invoice/detail";
    }

    @PostMapping("/{orderId}/send-email")
    public String resendEmail(@PathVariable Integer orderId, RedirectAttributes redirectAttributes) {
        try {
            invoiceService.resendInvoiceEmail(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi email hóa đơn cho khách hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể gửi email: " + e.getMessage());
            log.error("Error sending invoice email for order {}: {}", orderId, e.getMessage(), e);
        }
        return "redirect:/cashier/invoices/" + orderId;
    }
}
