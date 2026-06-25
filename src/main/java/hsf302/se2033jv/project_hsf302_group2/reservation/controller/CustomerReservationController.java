package hsf302.se2033jv.project_hsf302_group2.reservation.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CancelReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.response.ReservationResponse;
import hsf302.se2033jv.project_hsf302_group2.reservation.service.interfaces.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer/reservations")
@RequiredArgsConstructor
public class CustomerReservationController {

    private final ReservationService reservationService;
    private final ProfileService profileService;

    // GET /customer/reservations — danh sách đặt bàn
    @GetMapping
    public String showMyReservations(Model model, Authentication auth) {

        User user = profileService.getCurrentUser(auth.getName());
        List<ReservationResponse> reservations = reservationService.getMyReservations(user.getUserId());

        model.addAttribute("user", user);
        model.addAttribute("reservations", reservations);
        return "reservation/my-reservations";
    }

    // GET /customer/reservations/{reservationId}/cancel — trang xác nhận hủy
    @GetMapping("/{reservationId}/cancel")
    public String showCancelConfirm(
            @PathVariable Integer reservationId,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = profileService.getCurrentUser(auth.getName());

        try {
            ReservationResponse reservation =
                    reservationService.getReservationForCancel(reservationId, user.getUserId());

            if (!reservation.isCanCancel()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không thể hủy đặt bàn ở trạng thái hiện tại.");
                return "redirect:/customer/reservations";
            }

            model.addAttribute("user", user);
            model.addAttribute("reservation", reservation);
            model.addAttribute("cancelForm", new CancelReservationRequest());
            return "reservation/cancel-confirm";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/reservations";
        }
    }

    // POST /customer/reservations/cancel — xử lý hủy
    @PostMapping("/cancel")
    public String cancelReservation(
            @Valid @ModelAttribute("cancelForm") CancelReservationRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";

        User user = profileService.getCurrentUser(auth.getName());

        if (bindingResult.hasErrors()) {
            try {
                ReservationResponse reservation =
                        reservationService.getReservationForCancel(
                                request.getReservationId(), user.getUserId());
                model.addAttribute("user", user);
                model.addAttribute("reservation", reservation);
                model.addAttribute("cancelForm", request);
                return "reservation/cancel-confirm";
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/customer/reservations";
            }
        }

        try {
            reservationService.cancelReservation(request, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đặt bàn thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/reservations";
    }
}
