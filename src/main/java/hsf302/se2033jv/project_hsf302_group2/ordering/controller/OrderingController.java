package hsf302.se2033jv.project_hsf302_group2.ordering.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PaymentStatus;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CategoryRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PaymentMethodRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.request.OrderFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.response.OrderPosDetailResponse;
import hsf302.se2033jv.project_hsf302_group2.ordering.dto.response.OrderPosListResponse;
import hsf302.se2033jv.project_hsf302_group2.ordering.service.interfaces.OrderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'MANAGER')")
public class OrderingController {

    private final OrderingService orderingService;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    private static final int PAGE_SIZE = 10;

    @GetMapping("/create")
    public String showPosScreen(Model model, Authentication auth) {
        User user = null;
        if (auth != null && auth.getName() != null) {
            user = userRepository.findByUsername(auth.getName()).orElse(null);
        }
        String cashierName = user != null ? ((user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : "")).trim() : (auth != null ? auth.getName() : "Thu ngân");
        if (cashierName.isEmpty() && user != null) cashierName = user.getUsername();

        model.addAttribute("cashierName", cashierName);
        model.addAttribute("categories", categoryRepository.findAllActive());
        model.addAttribute("paymentMethods", paymentMethodRepository.findAll());
        return "ordering/pos";
    }

    @GetMapping({"/list", ""})
    public String showOrderList(
            @ModelAttribute("filter") OrderFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User user = null;
        if (auth != null && auth.getName() != null) {
            user = userRepository.findByUsername(auth.getName()).orElse(null);
        }
        String cashierName = user != null ? ((user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : "")).trim() : (auth != null ? auth.getName() : "Thu ngân");
        if (cashierName.isEmpty() && user != null) cashierName = user.getUsername();

        Page<OrderPosListResponse> orderPage = orderingService.getOrders(filter != null ? filter : new OrderFilterRequest(), page, PAGE_SIZE);

        model.addAttribute("cashierName", cashierName);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("filter", filter != null ? filter : new OrderFilterRequest());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("orderTypes", OrderType.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        model.addAttribute("paymentMethods", paymentMethodRepository.findAll());

        return "ordering/list";
    }

    @GetMapping("/detail/{id}")
    public String showOrderDetail(@PathVariable("id") Integer id, Model model) {
        OrderPosDetailResponse order = orderingService.getOrderDetail(id);
        model.addAttribute("order", order);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("paymentMethods", paymentMethodRepository.findAll());
        return "ordering/detail";
    }
}
