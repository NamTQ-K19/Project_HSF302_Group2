package hsf302.se2033jv.project_hsf302_group2.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(basePackages = "hsf302.se2033jv.project_hsf302_group2.reservation")
@Slf4j
public class ReservationExceptionHandler {

    @ExceptionHandler(InvalidReservationTimeException.class)
    public String handleInvalidReservationTime(InvalidReservationTimeException e, RedirectAttributes redirectAttributes) {
        log.error("Invalid reservation time: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/customer/reservations/new";
    }

    @ExceptionHandler(TableNotAvailableException.class)
    public String handleTableNotAvailable(TableNotAvailableException e, RedirectAttributes redirectAttributes) {
        log.error("Table not available: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/customer/reservations/new";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Reservation error: ", e);
        redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        return "redirect:/customer/reservations/new";
    }
}