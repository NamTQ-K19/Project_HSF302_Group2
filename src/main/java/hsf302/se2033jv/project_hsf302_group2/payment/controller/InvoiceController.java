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
@PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'MANAGER')")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ProfileService profileService;

    @GetMapping("/{orderId}")
    public String showInvoiceDetail(@PathVariable Integer orderId, Model model, Authentication auth) {
        User cashier = profileService.getCurrentUser(auth.getName());
        String cashierName = ((cashier.getFirstName() != null ? cashier.getFirstName() : "") + " "
                + (cashier.getLastName() != null ? cashier.getLastName() : "")).trim();
        if (cashierName.isEmpty()) cashierName = cashier.getUsername();

        model.addAttribute("cashierName", cashierName);
        model.addAttribute("invoice", invoiceService.getInvoice(orderId));
        return "cashier/invoice/detail";
    }
    
}
