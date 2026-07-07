package hsf302.se2033jv.project_hsf302_group2.reservation.controller;

import hsf302.se2033jv.project_hsf302_group2.common.cache.ReservationSessionData;
import hsf302.se2033jv.project_hsf302_group2.common.entity.CoffeeTable;
import hsf302.se2033jv.project_hsf302_group2.common.entity.MapEntity;
import hsf302.se2033jv.project_hsf302_group2.common.entity.PaymentMethod;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.exception.TableNotAvailableException;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CancelReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CreateReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.DepositPaymentRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.TableAvailabilityRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.response.*;
import hsf302.se2033jv.project_hsf302_group2.reservation.service.interfaces.ReservationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Slf4j
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

    @GetMapping("/new")
    public String showCreateReservationForm(Model model, Authentication auth) {
        User user = profileService.getCurrentUser(auth.getName());
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new CreateReservationRequest());
        }
        List<MapEntity> maps = reservationService.getAllMaps();
        model.addAttribute("maps", maps);
        model.addAttribute("user", user);
        model.addAttribute("minDate", LocalDate.now().plusDays(1));
        model.addAttribute("maxDate", LocalDate.now().plusDays(30));
        model.addAttribute("openingTime", "07:00");
        model.addAttribute("closingTime", "22:00");
        return "reservation/create-reservation";
    }

    @PostMapping("/new")
    public String createReservation(
            @Valid @ModelAttribute("request") CreateReservationRequest request,
            BindingResult bindingResult,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession session) {

        User user = profileService.getCurrentUser(auth.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("minDate", LocalDate.now().plusDays(1));
            model.addAttribute("maxDate", LocalDate.now().plusDays(30));
            model.addAttribute("openingTime", "07:00");
            model.addAttribute("closingTime", "22:00");
            return "reservation/create-reservation";
        }

        try {
            // KIỂM TRA BÀN TRỐNG (KHÔNG TẠO RESERVATION)
            TableAvailabilityRequest availabilityRequest = TableAvailabilityRequest.builder()
                    .reservationDate(request.getReservationDate())
                    .reservationTime(request.getReservationTime())
                    .partySize(request.getPartySize())
                    .build();

            TableAvailabilityResponse availability = reservationService.checkAvailability(availabilityRequest);

            if (!availability.getAvailable()) {
                throw new TableNotAvailableException("Không có bàn trống cho thời gian này");
            }

            // Kiểm tra bàn được chọn có trống không
            if (request.getSelectedTableId() != null) {
                boolean isSelectedAvailable = availability.getAvailableTables().stream()
                        .anyMatch(t -> t.getTableId().equals(request.getSelectedTableId()));
                if (!isSelectedAvailable) {
                    throw new TableNotAvailableException("Bàn đã chọn không còn trống");
                }
            } else {
                // Nếu chưa chọn bàn, tự động chọn bàn đầu tiên
                if (!availability.getAvailableTables().isEmpty()) {
                    request.setSelectedTableId(availability.getAvailableTables().get(0).getTableId());
                }
            }

            // LƯU VÀO SESSION THAY VÌ LƯU DB
            ReservationSessionData sessionData = ReservationSessionData.builder()
                    .customerId(user.getUserId())
                    .customerName(user.getFirstName() + " " + user.getLastName())
                    .reservationDate(request.getReservationDate())
                    .reservationTime(request.getReservationTime())
                    .partySize(request.getPartySize())
                    .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 120)
                    .selectedTableId(request.getSelectedTableId())
                    .note(request.getNote())
                    .depositAmount(new BigDecimal(50000))
                    .holdMinutes(10)
                    .build();

            session.setAttribute("reservationSession", sessionData);
            log.info("Reservation data saved to session for user: {}", user.getUsername());

            return "redirect:/customer/reservations/payment";

        } catch (TableNotAvailableException e) {
            bindingResult.reject("tableNotAvailable", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("minDate", LocalDate.now().plusDays(1));
            model.addAttribute("maxDate", LocalDate.now().plusDays(30));
            model.addAttribute("openingTime", "07:00");
            model.addAttribute("closingTime", "22:00");
            return "reservation/create-reservation";
        } catch (Exception e) {
            log.error("Error creating reservation: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/customer/reservations/new";
        }
    }

    @PostMapping("/check-availability")
    @ResponseBody
    public Map<String, Object> checkAvailability(@RequestBody TableAvailabilityRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            TableAvailabilityResponse result = reservationService.checkAvailability(request);
            response.put("success", true);
            response.put("data", result);
            response.put("message", result.getMessage());
        } catch (Exception e) {
            log.error("Error checking availability: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/tables")
    @ResponseBody
    public Map<String, Object> getAvailableTables(
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam Integer partySize) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            LocalDate reservationDate = LocalDate.parse(date);
            LocalTime reservationTime = LocalTime.parse(time);
            List<TableMapResponse> tables = reservationService.getTablesForMap(reservationDate, reservationTime, partySize);
            response.put("success", true);
            response.put("tables", tables);
        } catch (Exception e) {
            log.error("Error getting tables: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // TRANG THANH TOÁN (LẤY DỮ LIỆU TỪ SESSION)
    @GetMapping("/payment")
    public String showPaymentPage(
            Model model,
            Authentication auth,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = profileService.getCurrentUser(auth.getName());
        ReservationSessionData sessionData = (ReservationSessionData) session.getAttribute("reservationSession");

        if (sessionData == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đặt bàn. Vui lòng đặt lại.");
            return "redirect:/customer/reservations/new";
        }

        // Thêm phương thức thanh toán
        List<PaymentMethod> paymentMethods = reservationService.getAllPaymentMethods();

        model.addAttribute("user", user);
        model.addAttribute("sessionData", sessionData);
        model.addAttribute("depositAmount", sessionData.getDepositAmount());
        model.addAttribute("holdMinutes", sessionData.getHoldMinutes());
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("pageTitle", "Thanh toán tiền cọc");

        return "reservation/payment-deposit-session";
    }

    // XÁC NHẬN THANH TOÁN VÀ TẠO RESERVATION
    @PostMapping("/payment/confirm")
    public String confirmPayment(
            @RequestParam Integer paymentMethodId,
            @RequestParam(required = false) String transactionRef,
            Authentication auth,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = profileService.getCurrentUser(auth.getName());
        ReservationSessionData sessionData = (ReservationSessionData) session.getAttribute("reservationSession");

        if (sessionData == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đặt bàn. Vui lòng đặt lại.");
            return "redirect:/customer/reservations/new";
        }

        try {
            // KIỂM TRA BÀN VẪN CÒN TRỐNG
            TableAvailabilityRequest availabilityRequest = TableAvailabilityRequest.builder()
                    .reservationDate(sessionData.getReservationDate())
                    .reservationTime(sessionData.getReservationTime())
                    .partySize(sessionData.getPartySize())
                    .build();

            TableAvailabilityResponse availability = reservationService.checkAvailability(availabilityRequest);

            if (!availability.getAvailable()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bàn đã được đặt trong lúc bạn thanh toán. Vui lòng đặt lại.");
                session.removeAttribute("reservationSession");
                return "redirect:/customer/reservations/new";
            }

            // TẠO RESERVATION
            CreateReservationRequest request = CreateReservationRequest.builder()
                    .reservationDate(sessionData.getReservationDate())
                    .reservationTime(sessionData.getReservationTime())
                    .partySize(sessionData.getPartySize())
                    .durationMinutes(sessionData.getDurationMinutes())
                    .selectedTableId(sessionData.getSelectedTableId())
                    .note(sessionData.getNote())
                    .build();

            ReservationConfirmationResponse response = reservationService.createReservation(request, user.getUserId());
            log.info("Reservation created with ID: {}", response.getReservation().getReservationId());

            // CHUYỂN SANG CONFIRMED NGAY LẬP TỨC (GIẢ ĐỊNH THANH TOÁN THÀNH CÔNG)
            MakeReservationResponse confirmedReservation = reservationService.confirmReservation(
                    response.getReservation().getReservationId()
            );
            log.info("Reservation {} confirmed after payment", confirmedReservation.getReservationId());

            // XÓA SESSION
            session.removeAttribute("reservationSession");

            // CHUYỂN ĐẾN TRANG XÁC NHẬN THÀNH CÔNG
            redirectAttributes.addFlashAttribute("reservation", confirmedReservation);
            redirectAttributes.addFlashAttribute("depositAmount", response.getDepositAmount());
            redirectAttributes.addFlashAttribute("successMessage", "Đặt bàn thành công!");

            return "redirect:/customer/reservations/" + confirmedReservation.getReservationId() + "/confirmation";

        } catch (TableNotAvailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bàn đã được đặt trong lúc bạn thanh toán. Vui lòng đặt lại.");
            session.removeAttribute("reservationSession");
            return "redirect:/customer/reservations/new";
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/customer/reservations/payment";
        }
    }

    // HỦY ĐẶT BÀN (XÓA SESSION)
    @GetMapping("/payment/cancel")
    public String cancelPayment(HttpSession session) {
        session.removeAttribute("reservationSession");
        return "redirect:/customer/reservations/new";
    }

    // CÁC METHOD CŨ GIỮ NGUYÊN
    @GetMapping("/{reservationId}/deposit")
    public String showDepositPage(
            @PathVariable Integer reservationId,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        User user = profileService.getCurrentUser(auth.getName());
        try {
            MakeReservationResponse reservation = reservationService.getReservationDetail(reservationId, user.getUserId());
            Map<Integer, String> paymentMethods = new LinkedHashMap<>();
            paymentMethods.put(1, "Tiền mặt");
            paymentMethods.put(2, "VNPay");
            paymentMethods.put(3, "Momo");
            paymentMethods.put(4, "ZaloPay");
            paymentMethods.put(5, "Thẻ Visa/Mastercard");
            model.addAttribute("user", user);
            model.addAttribute("reservation", reservation);
            model.addAttribute("depositAmount", 50000);
            model.addAttribute("paymentMethods", paymentMethods);
            return "reservation/payment-deposit";
        } catch (Exception e) {
            log.error("Error showing deposit page: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đặt bàn");
            return "redirect:/customer/reservations";
        }
    }

    @PostMapping("/{reservationId}/deposit")
    public String processDeposit(
            @PathVariable Integer reservationId,
            @RequestParam Integer paymentMethodId,
            @RequestParam(required = false) String transactionRef,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        User user = profileService.getCurrentUser(auth.getName());
        try {
            DepositPaymentRequest request = DepositPaymentRequest.builder()
                    .reservationId(reservationId)
                    .paymentMethodId(paymentMethodId)
                    .transactionRef(transactionRef != null ? transactionRef : "DEP-" + reservationId + "-" + System.currentTimeMillis())
                    .build();
            ReservationConfirmationResponse response = reservationService.payDeposit(request, user.getUserId());
            redirectAttributes.addFlashAttribute("reservation", response.getReservation());
            redirectAttributes.addFlashAttribute("depositAmount", response.getDepositAmount());
            return "redirect:/customer/reservations/" + reservationId + "/confirmation";
        } catch (Exception e) {
            log.error("Error processing deposit: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán thất bại: " + e.getMessage());
            return "redirect:/customer/reservations/" + reservationId + "/deposit";
        }
    }

    @GetMapping("/{reservationId}/confirmation")
    public String showConfirmation(
            @PathVariable Integer reservationId,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        User user = profileService.getCurrentUser(auth.getName());
        try {
            MakeReservationResponse reservation = reservationService.getReservationDetail(reservationId, user.getUserId());
            model.addAttribute("user", user);
            model.addAttribute("reservation", reservation);
            model.addAttribute("depositAmount", 50000);
            return "reservation/confirmation";
        } catch (Exception e) {
            log.error("Error showing confirmation: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đặt bàn");
            return "redirect:/customer/reservations";
        }
    }
}