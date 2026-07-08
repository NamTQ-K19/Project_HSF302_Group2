package hsf302.se2033jv.project_hsf302_group2.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

/**
 * Bắt exception cho toàn bộ package "customer" và trả về trang HTML thân thiện
 * (khác với GlobalExceptionHandler chỉ áp dụng cho "admin" và trả JSON)
 */
@Slf4j
@ControllerAdvice(basePackages = "hsf302.se2033jv.project_hsf302_group2.customer")
public class CustomerExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleBusinessException(BusinessException e, Model model) {
        log.warn("Business error (customer): {}", e.getMessage());
        model.addAttribute("errorTitle", "Không tìm thấy đơn hàng");
        model.addAttribute("errorMessage", "Đơn hàng không tồn tại hoặc bạn không có quyền xem đơn hàng này.");
        model.addAttribute("homeUrl", "/customer/orders/history");
        return "error/customer-error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException e, Model model) {
        log.warn("Type mismatch (customer): {}", e.getMessage());
        model.addAttribute("errorTitle", "Đường dẫn không hợp lệ");
        model.addAttribute("errorMessage", "Mã đơn hàng không hợp lệ.");
        model.addAttribute("homeUrl", "/customer/orders/history");
        return "error/customer-error";
    }

    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception e, Model model) {
        log.warn("Bad request (customer): {}", e.getMessage());
        model.addAttribute("errorTitle", "Yêu cầu không hợp lệ");
        model.addAttribute("errorMessage", "Dữ liệu bạn nhập không hợp lệ. Vui lòng thử lại.");
        model.addAttribute("homeUrl", "/customer/orders/history");
        return "error/customer-error";
    }
}