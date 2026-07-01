package hsf302.se2033jv.project_hsf302_group2.manager.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ReservationScheduleFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationDetailResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationScheduleResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReservationStatsResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ReservationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/manager/reservations")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('MANAGER')")
public class ReservationScheduleController {

    private static final int PAGE_SIZE = 10;

    private final ReservationScheduleService reservationScheduleService;
    private final ProfileService profileService;

    @GetMapping
    public String showSchedule(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User manager = profileService.getCurrentUser(auth.getName());

        // No filter by default - show all reservations
        ReservationScheduleFilterRequest filter = new ReservationScheduleFilterRequest();

        Page<ReservationScheduleResponse> reservationPage = reservationScheduleService
                .getSchedule(filter, page, PAGE_SIZE);

        ReservationStatsResponse stats = reservationScheduleService.getStats(filter.getFromDate(), filter.getToDate());

        model.addAttribute("manager", manager);
        model.addAttribute("reservationPage", reservationPage);
        model.addAttribute("reservations", reservationPage.getContent());
        model.addAttribute("filter", filter);
        model.addAttribute("stats", stats);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);

        return "manager/reservation/schedule";
    }

    @PostMapping("/filter")
    public String filterReservations(
            @ModelAttribute("filter") ReservationScheduleFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        User manager = profileService.getCurrentUser(auth.getName());

        // Validation: fromDate <= toDate
        if (filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {

            Page<ReservationScheduleResponse> reservationPage = reservationScheduleService
                    .getSchedule(filter, 0, PAGE_SIZE);

            // Stats: nếu có filter ngày, tính stats dựa trên filter
            ReservationStatsResponse stats = reservationScheduleService.getStats(filter.getFromDate(), filter.getToDate());

            model.addAttribute("manager", manager);
            model.addAttribute("reservationPage", reservationPage);
            model.addAttribute("reservations", reservationPage.getContent());
            model.addAttribute("filter", filter);
            model.addAttribute("stats", stats);
            model.addAttribute("currentPage", 0);
            model.addAttribute("pageSize", PAGE_SIZE);
            model.addAttribute("dateError", "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");

            return "manager/reservation/schedule";
        }

        Page<ReservationScheduleResponse> reservationPage = reservationScheduleService
                .getSchedule(filter, page, PAGE_SIZE);

        // Stats: nếu có filter ngày, tính stats dựa trên filter; nếu không, tính tất cả
        ReservationStatsResponse stats = reservationScheduleService.getStats(filter.getFromDate(), filter.getToDate());

        model.addAttribute("manager", manager);
        model.addAttribute("reservationPage", reservationPage);
        model.addAttribute("reservations", reservationPage.getContent());
        model.addAttribute("filter", filter);
        model.addAttribute("stats", stats);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);

        return "manager/reservation/schedule";
    }

    @GetMapping("/clear-filter")
    public String clearFilter() {
        return "redirect:/manager/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(
            @PathVariable Integer id,
            @RequestParam String cancellationReason,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            User manager = profileService.getCurrentUser(auth.getName());
            reservationScheduleService.cancelReservation(id, cancellationReason, manager.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đặt bàn thành công");
        } catch (RuntimeException e) {
            String errorMsg = "CANNOT_CANCEL_RESERVATION".equals(e.getMessage())
                    ? "Không thể hủy đặt bàn ở trạng thái này"
                    : "Lỗi hủy đặt bàn: " + e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            log.error("Error cancelling reservation: " + e.getMessage(), e);
        }

        return "redirect:/manager/reservations";
    }

    @GetMapping("/{id}")
    public String showReservationDetail(@PathVariable Integer id, Model model) {
        ReservationDetailResponse detail = reservationScheduleService.getReservationDetail(id);
        model.addAttribute("res", detail);
        return "manager/reservation/detail"; // Đường dẫn file HTML
    }
}

