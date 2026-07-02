package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AddressRedirectController {

    @GetMapping({"/customer/addresses/new", "/customer/address/new", "/addresses/new", "/customer/profile/addresses/new"})
    public String redirectToAddAddress() {
        return "redirect:/customer/profile?addAddress=true#addAddressForm";
    }
}
