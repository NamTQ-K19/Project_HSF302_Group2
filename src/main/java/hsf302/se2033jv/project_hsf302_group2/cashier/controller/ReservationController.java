package hsf302.se2033jv.project_hsf302_group2.cashier.controller;

import hsf302.se2033jv.project_hsf302_group2.cashier.dto.request.CashierReservationFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.request.CashierRefundRequest;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierReservationListResponse;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierReservationDetailResponse;
import hsf302.se2033jv.project_hsf302_group2.cashier.dto.response.CashierRefundResponse;
import hsf302.se2033jv.project_hsf302_group2.auth.security.CustomUserDetails;
import hsf302.se2033jv.project_hsf302_group2.auth.security.CustomOidcUser;
import hsf302.se2033jv.project_hsf302_group2.cashier.service.interfaces.ReservationTableService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@Controller
@RequestMapping("/cashier/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'MANAGER')")
public class ReservationController {

    private final ReservationTableService reservationTableService;

    private static final int PAGE_SIZE = 10;

    @GetMapping({"", "/"})
    public String listReservations(
            @ModelAttribute("filter") CashierReservationFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User user = null;
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                user = userDetails.getUser();
            } else if (principal instanceof CustomOidcUser oidcUser) {
                user = oidcUser.getUser();
            }
        }
        String cashierName = user != null ? ((user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : "")).trim() : (auth != null ? auth.getName() : "Thu ngân");
        if (cashierName.isEmpty() && user != null) {
            cashierName = user.getUsername();
        }

        if (filter.getKeyword() != null) {
            filter.setKeyword(filter.getKeyword().trim());
        }

        Page<CashierReservationListResponse> reservationPage = reservationTableService.getReservations(filter, page, PAGE_SIZE);

        model.addAttribute("cashierName", cashierName);
        model.addAttribute("reservationPage", reservationPage);
        model.addAttribute("reservations", reservationPage.getContent());
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);

        return "cashier/reservation-list";
    }

    @GetMapping("/detail/{id}")
    public String getReservationDetail(@PathVariable("id") Integer id, Model model) {
        CashierReservationDetailResponse reservation = reservationTableService.getReservationDetail(id);
        model.addAttribute("reservation", reservation);
        return "cashier/reservation-detail-modal";
    }

    @PostMapping("/refund")
    @ResponseBody
    public ResponseEntity<CashierRefundResponse> refundDeposit(
            @Valid @RequestBody CashierRefundRequest request,
            Authentication auth,
            HttpServletRequest servletRequest) {
        
        String username = auth != null ? auth.getName() : "cashier";
        String ipAddress = VNPayUtil.getIpAddress(servletRequest);
        
        CashierRefundResponse response = reservationTableService.processRefund(request, username, ipAddress);
        return ResponseEntity.ok(response);
    }
}
