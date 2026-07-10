package hsf302.se2033jv.project_hsf302_group2.reservation.controller;

import hsf302.se2033jv.project_hsf302_group2.common.cache.ReservationSessionData;
import hsf302.se2033jv.project_hsf302_group2.common.entity.CoffeeTable;
import hsf302.se2033jv.project_hsf302_group2.common.entity.MapEntity;
import hsf302.se2033jv.project_hsf302_group2.common.entity.PaymentMethod;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus;
import hsf302.se2033jv.project_hsf302_group2.common.exception.InvalidReservationTimeException;
import hsf302.se2033jv.project_hsf302_group2.common.exception.TableNotAvailableException;
import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.ConfigService;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CancelReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CreateReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.DepositPaymentRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.TableAvailabilityRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.response.*;
import hsf302.se2033jv.project_hsf302_group2.reservation.service.interfaces.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
    private final ConfigService configService;

    // Danh sach dat ban cua khach hang
    @GetMapping
    public String showMyReservations(Model model, Authentication auth) {

        User user = profileService.getCurrentUser(auth.getName());
        List<ReservationResponse> reservations = reservationService.getMyReservations(user.getUserId());

        model.addAttribute("user", user);
        model.addAttribute("reservations", reservations);
        return "reservation/my-reservations";
    }

    // Trang xac nhan huy dat ban
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
                        "Khong the huy dat ban o trang thai hien tai.");
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

    // Xu ly huy dat ban
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
            redirectAttributes.addFlashAttribute("successMessage", "Huy dat ban thanh cong!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/reservations";
    }

    // Trang tao dat ban moi
    @GetMapping("/new")
    public String showCreateReservationForm(Model model, Authentication auth) {
        User user = profileService.getCurrentUser(auth.getName());
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new CreateReservationRequest());
        }
        List<MapEntity> maps = reservationService.getAllMaps();
        model.addAttribute("maps", maps);
        model.addAttribute("user", user);
        model.addAttribute("minDate", LocalDate.now());
        model.addAttribute("maxDate", LocalDate.now().plusDays(30));
        model.addAttribute("openingTime", "07:00");
        model.addAttribute("closingTime", "22:00");
        return "reservation/create-reservation";
    }

    // Xu ly tao dat ban
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
            model.addAttribute("minDate", LocalDate.now());
            model.addAttribute("maxDate", LocalDate.now().plusDays(30));
            model.addAttribute("openingTime", "07:00");
            model.addAttribute("closingTime", "22:00");
            return "reservation/create-reservation";
        }

        try {
            TableAvailabilityRequest availabilityRequest = TableAvailabilityRequest.builder()
                    .reservationDate(request.getReservationDate())
                    .reservationTime(request.getReservationTime())
                    .partySize(request.getPartySize())
                    .build();

            TableAvailabilityResponse availability = reservationService.checkAvailability(availabilityRequest);

            if (!availability.getAvailable()) {
                throw new TableNotAvailableException("Không có bàn trống trong thời gian này");
            }

            if (request.getSelectedTableId() != null) {
                boolean isSelectedAvailable = availability.getAvailableTables().stream()
                        .anyMatch(t -> t.getTableId().equals(request.getSelectedTableId()));
                if (!isSelectedAvailable) {
                    throw new TableNotAvailableException("Bàn đã chọn không còn trống");
                }
            } else {
                if (!availability.getAvailableTables().isEmpty()) {
                    request.setSelectedTableId(availability.getAvailableTables().get(0).getTableId());
                }
            }

            // TẠO RESERVATION TRƯỚC (status = PENDING)
            CreateReservationRequest createRequest = CreateReservationRequest.builder()
                    .reservationDate(request.getReservationDate())
                    .reservationTime(request.getReservationTime())
                    .partySize(request.getPartySize())
                    .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 120)
                    .selectedTableId(request.getSelectedTableId())
                    .note(request.getNote())
                    .build();

            ReservationConfirmationResponse response = reservationService.createReservation(createRequest, user.getUserId());
            Integer reservationId = response.getReservation().getReservationId();

            // 2. LẤY SỐ TIỀN CỌC
            long depositAmount = configService.getReservationDepositAmount();
            int holdMinutes = configService.getReservationHoldMinutes();

            // 3. LƯU VÀO SESSION VỚI reservationId
            ReservationSessionData sessionData = ReservationSessionData.builder()
                    .reservationId(reservationId)
                    .customerId(user.getUserId())
                    .customerName(user.getFirstName() + " " + user.getLastName())
                    .reservationDate(request.getReservationDate())
                    .reservationTime(request.getReservationTime())
                    .partySize(request.getPartySize())
                    .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 120)
                    .selectedTableId(request.getSelectedTableId())
                    .note(request.getNote())
                    .depositAmount(BigDecimal.valueOf(depositAmount))
                    .holdMinutes(holdMinutes)
                    .build();

            session.setAttribute("reservationSession", sessionData);

            return "redirect:/customer/reservations/payment";

        } catch (InvalidReservationTimeException e) {
            bindingResult.reject("invalidTime", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("minDate", LocalDate.now());
            model.addAttribute("maxDate", LocalDate.now().plusDays(30));
            model.addAttribute("openingTime", "07:00");
            model.addAttribute("closingTime", "22:00");
            return "reservation/create-reservation";
        } catch (TableNotAvailableException e) {
            bindingResult.reject("tableNotAvailable", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("minDate", LocalDate.now());
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

    // Kiem tra ban trong
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

    // Lay danh sach ban theo ban do
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

    // Trang thanh toan
    @GetMapping("/payment")
    public String showPaymentPage(
            Model model,
            Authentication auth,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = profileService.getCurrentUser(auth.getName());
        ReservationSessionData sessionData = (ReservationSessionData) session.getAttribute("reservationSession");

        if (sessionData == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đặt bàn. Vui lòng thử lại");
            return "redirect:/customer/reservations/new";
        }

        BigDecimal depositAmount = sessionData.getDepositAmount();
        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) == 0) {
            depositAmount = BigDecimal.valueOf(configService.getReservationDepositAmount());
            sessionData.setDepositAmount(depositAmount);
            session.setAttribute("reservationSession", sessionData);
        }

        int holdMinutes = sessionData.getHoldMinutes();
        if (holdMinutes <= 0) {
            holdMinutes = configService.getReservationHoldMinutes();
            sessionData.setHoldMinutes(holdMinutes);
            session.setAttribute("reservationSession", sessionData);
        }

        List<PaymentMethod> paymentMethods = reservationService.getAllPaymentMethods();

        model.addAttribute("user", user);
        model.addAttribute("sessionData", sessionData);
        model.addAttribute("depositAmount", depositAmount);
        model.addAttribute("holdMinutes", holdMinutes);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("pageTitle", "Thanh toán tiền cọc");

        return "reservation/payment-deposit-session";
    }

    // Xac nhan thanh toan va tao reservation
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
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đặt bàn. Vui lòng thử lại");
            return "redirect:/customer/reservations/new";
        }

        try {
            TableAvailabilityRequest availabilityRequest = TableAvailabilityRequest.builder()
                    .reservationDate(sessionData.getReservationDate())
                    .reservationTime(sessionData.getReservationTime())
                    .partySize(sessionData.getPartySize())
                    .build();

            TableAvailabilityResponse availability = reservationService.checkAvailability(availabilityRequest);

            if (!availability.getAvailable()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bàn đã được đặt trong lúc thanh toán. Vui lòng đặt lại.");
                session.removeAttribute("reservationSession");
                return "redirect:/customer/reservations/new";
            }

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

            MakeReservationResponse confirmedReservation = reservationService.confirmReservation(
                    response.getReservation().getReservationId()
            );
            log.info("Reservation {} confirmed after payment", confirmedReservation.getReservationId());

            session.removeAttribute("reservationSession");

            redirectAttributes.addFlashAttribute("reservation", confirmedReservation);
            redirectAttributes.addFlashAttribute("depositAmount", response.getDepositAmount());
            redirectAttributes.addFlashAttribute("successMessage", "Đặt bàn thành công!");

            return "redirect:/customer/reservations/" + confirmedReservation.getReservationId() + "/confirmation";

        } catch (TableNotAvailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bàn đã được đặt trong lúc thanh toán. Vui lòng đặt lại");
            session.removeAttribute("reservationSession");
            return "redirect:/customer/reservations/new";
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/customer/reservations/payment";
        }
    }

    // Huy thanh toan
    @GetMapping("/payment/cancel")
    public String cancelPayment(HttpSession session) {
        session.removeAttribute("reservationSession");
        return "redirect:/customer/reservations/new";
    }

    @GetMapping("/{reservationId}/deposit")
    public String redirectDepositToPayment(
            @PathVariable Integer reservationId) {
        return "redirect:/customer/reservations/" + reservationId + "/payment";
    }

    @PostMapping("/{reservationId}/deposit")
    public String processDeposit(
            @PathVariable Integer reservationId,
            @RequestParam Integer paymentMethodId,
            @RequestParam(required = false) String transactionRef,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        User user = profileService.getCurrentUser(auth.getName());

        try {
            DepositPaymentRequest depositRequest = DepositPaymentRequest.builder()
                    .reservationId(reservationId)
                    .paymentMethodId(paymentMethodId)
                    .transactionRef(transactionRef != null ? transactionRef : "DEP-" + reservationId + "-" + System.currentTimeMillis())
                    .build();

            ReservationConfirmationResponse response = reservationService.payDeposit(depositRequest, user.getUserId());

            if (response.getPaymentUrl() != null && !response.getPaymentUrl().isEmpty()) {
                return "redirect:" + response.getPaymentUrl();
            }

            redirectAttributes.addFlashAttribute("reservation", response.getReservation());
            redirectAttributes.addFlashAttribute("depositAmount", response.getDepositAmount());
            redirectAttributes.addFlashAttribute("successMessage", response.getMessage());
            return "redirect:/customer/reservations/" + reservationId + "/confirmation";

        } catch (Exception e) {
            log.error("Error processing deposit: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán thất bại: " + e.getMessage());
            return "redirect:/customer/reservations/" + reservationId + "/deposit";
        }
    }

    // Trang xac nhan dat ban thanh cong
    @GetMapping("/{reservationId}/confirmation")
    public String showConfirmation(
            @PathVariable Integer reservationId,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        User user = profileService.getCurrentUser(auth.getName());
        try {
            MakeReservationResponse reservation = reservationService.getReservationDetail(reservationId, user.getUserId());
            long depositAmount = configService.getReservationDepositAmount();
            model.addAttribute("user", user);
            model.addAttribute("reservation", reservation);
            model.addAttribute("depositAmount", depositAmount);
            return "reservation/confirmation";
        } catch (Exception e) {
            log.error("Error showing confirmation: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đặt bàn");
            return "redirect:/customer/reservations";
        }
    }

    //Chuyển đến payment-deposit-session khi thanh toán lại
    @GetMapping("/{reservationId}/payment")
    public String showRetryPaymentPage(
            @PathVariable Integer reservationId,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = profileService.getCurrentUser(auth.getName());

        try {
            // Kiểm tra quyền sở hữu
            MakeReservationResponse reservation = reservationService.getReservationDetail(reservationId, user.getUserId());

            // Kiểm tra trạng thái có thể thanh toán lại
            if (reservation.getStatus() != ReservationStatus.PENDING) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Đặt bàn không ở trạng thái chờ thanh toán");
                return "redirect:/customer/reservations";
            }

            // Kiểm tra deposit chưa thanh toán
            if (reservation.getDepositStatus() != null &&
                    "PAID".equals(reservation.getDepositStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Tiền cọc đã được thanh toán");
                return "redirect:/customer/reservations";
            }

            // Lấy danh sách phương thức thanh toán
            List<PaymentMethod> paymentMethods = reservationService.getAllPaymentMethods();

            // Lấy thông tin deposit
            ReservationDepositInfo depositInfo = reservationService.getDepositByReservationId(reservationId);

            // Tạo session data để dùng chung với payment-deposit-session
            ReservationSessionData sessionData = ReservationSessionData.builder()
                    .reservationId(reservationId)
                    .customerId(user.getUserId())
                    .customerName(user.getFirstName() + " " + user.getLastName())
                    .reservationDate(reservation.getReservationDate())
                    .reservationTime(reservation.getReservationTime())
                    .partySize(reservation.getPartySize())
                    .durationMinutes(reservation.getDurationMinutes())
                    .selectedTableId(reservation.getTableIds() != null && !reservation.getTableIds().isEmpty()
                            ? reservation.getTableIds().get(0) : null)
                    .note(reservation.getNote())
                    .depositAmount(depositInfo != null ? depositInfo.getDepositAmount() :
                            BigDecimal.valueOf(configService.getReservationDepositAmount()))
                    .holdMinutes(configService.getReservationHoldMinutes())
                    .build();

            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest().getSession();
            session.setAttribute("reservationSession", sessionData);

            model.addAttribute("user", user);
            model.addAttribute("sessionData", sessionData);
            model.addAttribute("depositAmount", sessionData.getDepositAmount());
            model.addAttribute("holdMinutes", sessionData.getHoldMinutes());
            model.addAttribute("paymentMethods", paymentMethods);
            model.addAttribute("pageTitle", "Thanh toán lại tiền cọc");
            model.addAttribute("isRetry", true);

            return "reservation/payment-deposit-session";

        } catch (Exception e) {
            log.error("Error showing retry payment page: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đặt bàn");
            return "redirect:/customer/reservations";
        }
    }
}