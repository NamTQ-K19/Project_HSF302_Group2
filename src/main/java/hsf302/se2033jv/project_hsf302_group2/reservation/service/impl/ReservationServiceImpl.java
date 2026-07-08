package hsf302.se2033jv.project_hsf302_group2.reservation.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.*;
import hsf302.se2033jv.project_hsf302_group2.common.enums.*;
import hsf302.se2033jv.project_hsf302_group2.common.exception.*;
import hsf302.se2033jv.project_hsf302_group2.common.repository.*;
import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.ConfigService;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CancelReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.CreateReservationRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.DepositPaymentRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.request.TableAvailabilityRequest;
import hsf302.se2033jv.project_hsf302_group2.reservation.dto.response.*;
import hsf302.se2033jv.project_hsf302_group2.reservation.service.interfaces.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static hsf302.se2033jv.project_hsf302_group2.common.enums.ReservationStatus.CANCELLED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationDepositRepository reservationDepositRepository;
    private final CoffeeTableRepository tableRepository;
    private final ReservationDepositRepository depositRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SystemLogRepository systemLogRepository;
    private final ConfigService configService;
    private final OrderRepository orderRepository;
    private final MapRepository mapRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public List<ReservationResponse> getMyReservations(Integer customerId) {
        return reservationRepository
                .findByCustomer_UserIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationResponse getReservationForCancel(Integer reservationId, Integer customerId) {
        Reservation reservation = reservationRepository
                .findByReservationIdAndCustomer_UserId(reservationId, customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));
        return mapToDTO(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(CancelReservationRequest request, Integer customerId) {

        // 1. Kiểm tra quyền sở hữu
        Reservation reservation = reservationRepository
                .findByReservationIdAndCustomer_UserId(request.getReservationId(), customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));

        // 2. Kiểm tra trạng thái
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Không thể hủy đặt bàn ở trạng thái: "
                    + reservation.getStatus().getValue());
        }

        // 3. Xử lý deposit nếu có
        reservationDepositRepository
                .findByReservation_ReservationId(reservation.getReservationId())
                .ifPresent(deposit -> {
                    if (deposit.getPaymentStatus() == DepositPaymentStatus.PAID) {
                        deposit.setPaymentStatus(DepositPaymentStatus.FORFEITED);
                    } else {
                        deposit.setPaymentStatus(DepositPaymentStatus.CANCELLED);
                    }
                    reservationDepositRepository.save(deposit);
                });

        // 4. Cập nhật reservation
        reservation.setStatus(CANCELLED);
        reservation.setCancelledBy(User.builder().userId(customerId).build());
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(request.getCancellationReason());
        reservationRepository.save(reservation);
//
//        // 5. Giải phóng bàn
//        if (reservation.getTables() != null) {
//            reservation.getTables().forEach(table ->
//                    table.setStatus(TableStatus.AVAILABLE)
//            );
//        }
    }

    @Override
    public TableAvailabilityResponse checkAvailability(TableAvailabilityRequest request) {
        validateDateTime(request.getReservationDate(), request.getReservationTime());
        validatePartySize(request.getPartySize());

        List<Reservation> activeReservations = reservationRepository.findActiveReservationsByDate(request.getReservationDate());

        Set<Integer> reservedTableIds = activeReservations.stream()
                .filter(r -> {
                    LocalTime start = r.getReservationTime();
                    int duration = r.getDurationMinutes() != null ? r.getDurationMinutes() : 120;
                    LocalTime end = start.plusMinutes(duration);
                    return !request.getReservationTime().isBefore(start) && request.getReservationTime().isBefore(end);
                })
                .flatMap(r -> r.getTables().stream())
                .map(CoffeeTable::getTableId)
                .collect(Collectors.toSet());

        List<Integer> activeOrderTableIds = orderRepository.findActiveOrderTableIds();

        Set<Integer> occupiedTableIds = new HashSet<>();
        occupiedTableIds.addAll(reservedTableIds);
        occupiedTableIds.addAll(activeOrderTableIds);

        List<CoffeeTable> allTables = tableRepository.findAllActiveTables();

        List<CoffeeTable> availableTables = allTables.stream()
                .filter(table -> table.getCapacity() >= request.getPartySize())
                .filter(table -> !occupiedTableIds.contains(table.getTableId()))
                .collect(Collectors.toList());

        boolean available = !availableTables.isEmpty();

        TableAvailabilityResponse response = TableAvailabilityResponse.builder()
                .available(available)
                .totalAvailable(availableTables.size())
                .requiredCapacity(request.getPartySize())
                .build();

        if (available) {
            response.setMessage("Còn " + availableTables.size() + " bàn trống cho thời gian này");
            response.setAvailableTables(availableTables.stream()
                    .map(this::convertToTableInfo)
                    .collect(Collectors.toList()));
            response.setSuggestedTables(availableTables.stream()
                    .limit(3)
                    .map(this::convertToTableInfo)
                    .collect(Collectors.toList()));
        } else {
            String suggestion = findAlternativeTime(request);
            response.setMessage("Không còn bàn trống. " + suggestion);
        }

        return response;
    }

    @Override
    @Transactional
    public ReservationConfirmationResponse createReservation(CreateReservationRequest request, Integer customerId) {
        try {
            int maxPerDay = configService.getReservationMaxPerDay();
            long depositAmount = configService.getReservationDepositAmount();
            int holdMinutes = configService.getReservationHoldMinutes();

            int count = reservationRepository.countReservationsByCustomerAndDate(customerId, request.getReservationDate());
            if (count >= maxPerDay) {
                throw new ReservationException("Bạn đã đạt giới hạn " + maxPerDay + " lần đặt bàn trong ngày");
            }

            validateDateTime(request.getReservationDate(), request.getReservationTime());
            validatePartySize(request.getPartySize());

            List<Reservation> activeReservations = reservationRepository.findActiveReservationsByDate(request.getReservationDate());

            Set<Integer> reservedTableIds = activeReservations.stream()
                    .filter(r -> {
                        LocalTime start = r.getReservationTime();
                        int duration = r.getDurationMinutes() != null ? r.getDurationMinutes() : 120;
                        LocalTime end = start.plusMinutes(duration);
                        return !request.getReservationTime().isBefore(start) && request.getReservationTime().isBefore(end);
                    })
                    .flatMap(r -> r.getTables().stream())
                    .map(CoffeeTable::getTableId)
                    .collect(Collectors.toSet());

            List<Integer> activeOrderTableIds = orderRepository.findActiveOrderTableIds();

            Set<Integer> occupiedTableIds = new HashSet<>();
            occupiedTableIds.addAll(reservedTableIds);
            occupiedTableIds.addAll(activeOrderTableIds);
            List<CoffeeTable> allTables = tableRepository.findAllActiveTables();

            List<CoffeeTable> availableTables = allTables.stream()
                    .filter(table -> table.getCapacity() >= request.getPartySize())
                    .filter(table -> !occupiedTableIds.contains(table.getTableId()))
                    .collect(Collectors.toList());

            if (availableTables.isEmpty()) {
                throw new TableNotAvailableException("Không có bàn trống cho thời gian và số lượng khách này");
            }

            CoffeeTable selectedTable = null;
            if (request.getSelectedTableId() != null) {
                selectedTable = availableTables.stream()
                        .filter(t -> t.getTableId().equals(request.getSelectedTableId()))
                        .findFirst()
                        .orElseThrow(() -> new TableNotAvailableException("Bàn đã chọn không còn trống hoặc không phù hợp"));
                log.info("User selected table: {}", selectedTable.getTableId());
            } else {
                selectedTable = findBestTable(availableTables, request.getPartySize());
            }

            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            Order tempOrder = Order.builder()
                    .user(customer)
                    .orderType(OrderType.COUNTER)
                    .orderStatus(OrderStatus.PENDING)
                    .subtotal(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .shippingFee(BigDecimal.ZERO)
                    .pointsEarned(0)
                    .build();
            Order savedOrder = orderRepository.save(tempOrder);

            Reservation reservation = Reservation.builder()
                    .customer(customer)
                    .partySize(request.getPartySize())
                    .reservationDate(request.getReservationDate())
                    .reservationTime(request.getReservationTime())
                    .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 120)
                    .status(ReservationStatus.PENDING)
                    .note(request.getNote())
                    .order(savedOrder)
                    .build();

            if (reservation.getTables() == null) {
                reservation.setTables(new HashSet<>());
            }

            reservation.getTables().add(selectedTable);
            Reservation savedReservation = reservationRepository.save(reservation);

            BigDecimal depositAmountDecimal = BigDecimal.valueOf(depositAmount);
            ReservationDeposit deposit = ReservationDeposit.builder()
                    .reservation(savedReservation)
                    .depositAmount(depositAmountDecimal)
                    .paymentStatus(DepositPaymentStatus.PENDING)
                    .refundAmount(BigDecimal.ZERO)
                    .appliedToOrder(false)
                    .build();
            depositRepository.save(deposit);

            logSystemAction(customer, "CREATE_RESERVATION", "reservations",
                    savedReservation.getReservationId(), "Tạo đặt bàn cho " + customer.getUsername());

            return buildConfirmationResponse(savedReservation, selectedTable, depositAmountDecimal, holdMinutes);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public MakeReservationResponse getReservationDetail(Integer reservationId, Integer customerId) {
        Reservation reservation = reservationRepository
                .findByReservationIdAndCustomerUserId(reservationId, customerId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        return convertToResponse(reservation);
    }

    @Override
    public Page<MakeReservationResponse> getCustomerReservations(Integer customerId, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findByCustomerUserId(customerId, pageable);
        return reservations.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public MakeReservationResponse cancelReservation(Integer reservationId, Integer customerId, String reason) {
        log.info("Cancelling reservation {} by customer {}", reservationId, customerId);

        Reservation reservation = reservationRepository
                .findByReservationIdAndCustomerUserId(reservationId, customerId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationException("Đặt bàn đã được hủy trước đó");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new ReservationException("Không thể hủy đặt bàn đã hoàn thành");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancellationReason(reason);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancelledBy(userRepository.findById(customerId).orElse(null));

        releaseTables(reservation);

        ReservationDeposit deposit = depositRepository.findByReservationReservationId(reservationId).orElse(null);
        if (deposit != null && deposit.getPaymentStatus() == DepositPaymentStatus.PAID) {
            deposit.setPaymentStatus(DepositPaymentStatus.FORFEITED);
            deposit.setRefundNote("Hủy đặt bàn - Không hoàn tiền cọc theo chính sách");
            depositRepository.save(deposit);
        }

        reservationRepository.save(reservation);

        User customer = userRepository.findById(customerId).orElse(null);
        logSystemAction(customer, "CANCEL_RESERVATION", "reservations",
                reservationId, "Hủy đặt bàn: " + reason);

        return convertToResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationConfirmationResponse payDeposit(DepositPaymentRequest request, Integer customerId) {
        log.info("Processing deposit payment for reservation {}", request.getReservationId());

        Reservation reservation = reservationRepository
                .findByReservationIdAndCustomerUserId(request.getReservationId(), customerId)
                .orElseThrow(() -> new ReservationNotFoundException(request.getReservationId()));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ReservationException("Đặt bàn không ở trạng thái chờ thanh toán");
        }

        ReservationDeposit deposit = depositRepository.findByReservationReservationId(request.getReservationId())
                .orElseThrow(() -> new ReservationException("Không tìm thấy thông tin tiền cọc"));

        if (deposit.getPaymentStatus() == DepositPaymentStatus.PAID) {
            throw new ReservationException("Tiền cọc đã được thanh toán");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ"));

        boolean paymentSuccess = processPayment(request);

        if (!paymentSuccess) {
            releaseTables(reservation);
            throw new DepositPaymentFailedException("Thanh toán thất bại. Bàn đã được giải phóng.");
        }

        deposit.setPaymentStatus(DepositPaymentStatus.PAID);
        deposit.setPaymentMethod(paymentMethod);
        deposit.setTransactionRef(request.getTransactionRef());
        depositRepository.save(deposit);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        User customer = userRepository.findById(customerId).orElse(null);
        logSystemAction(customer, "PAY_DEPOSIT", "reservation_deposits",
                deposit.getDepositId(), "Thanh toán tiền cọc đặt bàn thành công");

        int holdMinutes = configService.getReservationHoldMinutes();
        return buildConfirmationResponse(reservation,
                reservation.getTables().iterator().next(),
                deposit.getDepositAmount(),
                holdMinutes);
    }

    @Override
    @Transactional
    public MakeReservationResponse confirmReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() == ReservationStatus.PENDING) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            log.info("Reservation {} confirmed successfully", reservationId);

            ReservationDeposit deposit = depositRepository.findByReservationReservationId(reservationId).orElse(null);
            if (deposit != null && deposit.getPaymentStatus() == DepositPaymentStatus.PENDING) {
                deposit.setPaymentStatus(DepositPaymentStatus.PAID);
                depositRepository.save(deposit);
                log.info("Deposit {} marked as PAID", deposit.getDepositId());
            }
        }

        return convertToResponse(reservation);
    }

    @Override
    @Transactional
    public void releaseExpiredHolds() {
        int holdMinutes = configService.getReservationHoldMinutes();
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(holdMinutes);
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndCreatedAtBefore(ReservationStatus.PENDING, expiryTime);

        for (Reservation reservation : expiredReservations) {
            releaseTables(reservation);
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setCancellationReason("Hết thời gian giữ bàn (" + holdMinutes + " phút)");
            reservation.setCancelledAt(LocalDateTime.now());
            reservationRepository.save(reservation);
            log.info("Released expired hold for reservation {}", reservation.getReservationId());
        }
    }

    @Override
    public MakeReservationResponse getReservationStatus(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        return convertToResponse(reservation);
    }

    @Override
    public List<TableMapResponse> getTablesForMap(LocalDate date, LocalTime time, Integer partySize) {
        log.info("=== getTablesForMap ===");
        log.info("Date: {}, Time: {}, PartySize: {}", date, time, partySize);

        List<Reservation> activeReservations = reservationRepository.findActiveReservationsByDate(date);
        log.info("Active reservations in date: {}", activeReservations.size());

        Set<Integer> reservedTableIds = activeReservations.stream()
                .filter(r -> {
                    LocalTime start = r.getReservationTime();
                    int duration = r.getDurationMinutes() != null ? r.getDurationMinutes() : 120;
                    LocalTime end = start.plusMinutes(duration);
                    return !time.isBefore(start) && time.isBefore(end);
                })
                .flatMap(r -> r.getTables().stream())
                .map(CoffeeTable::getTableId)
                .collect(Collectors.toSet());

        log.info("!!! RESERVED TABLE IDs (filtered by Java): {}", reservedTableIds);

        List<Integer> activeOrderTableIds = orderRepository.findActiveOrderTableIds();
        log.info("!!! ACTIVE ORDER TABLE IDs: {}", activeOrderTableIds);

        Set<Integer> occupiedTableIds = new HashSet<>();
        occupiedTableIds.addAll(reservedTableIds);
        occupiedTableIds.addAll(activeOrderTableIds);
        log.info("!!! OCCUPIED TABLE IDs: {}", occupiedTableIds);

        List<CoffeeTable> allTables = tableRepository.findAllActiveTables();

        return allTables.stream()
                .filter(table -> table.getCapacity() >= partySize)
                .map(table -> TableMapResponse.builder()
                        .tableId(table.getTableId())
                        .capacity(table.getCapacity())
                        .isAvailable(!occupiedTableIds.contains(table.getTableId()))
                        .tableName("Bàn " + table.getTableId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<MapEntity> getAllMaps() {
        log.info("Getting all maps");
        return mapRepository.findAllByOrderByMapIdAsc();
    }

    @Override
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll();
    }

    // ==================== PRIVATE METHODS ====================

    private ReservationResponse mapToDTO(Reservation r) {
        ReservationDeposit deposit = reservationDepositRepository
                .findByReservation_ReservationId(r.getReservationId())
                .orElse(null);

        ReservationResponse dto = new ReservationResponse();
        dto.setReservationId(r.getReservationId());
        dto.setReservationDate(r.getReservationDate());
        dto.setReservationTime(r.getReservationTime());
        dto.setPartySize(r.getPartySize());
        dto.setDurationMinutes(r.getDurationMinutes());
        dto.setStatus(r.getStatus());
        dto.setStatusVi(getStatusVi(r.getStatus()));
        dto.setNote(r.getNote());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setCanCancel(r.getStatus() == ReservationStatus.PENDING
                || r.getStatus() == ReservationStatus.CONFIRMED);

        if (deposit != null) {
            dto.setDepositAmount(deposit.getDepositAmount());
            dto.setDepositStatus(deposit.getPaymentStatus());
            dto.setDepositStatusVi(getDepositStatusVi(deposit.getPaymentStatus()));
        }

        if (r.getTables() != null) {
            dto.setTableIds(r.getTables().stream()
                    .map(CoffeeTable::getTableId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private String getStatusVi(ReservationStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING:   return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case ARRIVED:   return "Đã đến";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            case NO_SHOW:   return "Không đến";
            default:        return status.getValue();
        }
    }

    private String getDepositStatusVi(DepositPaymentStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING:   return "Chưa thanh toán";
            case PAID:      return "Đã thanh toán";
            case REFUNDED:  return "Đã hoàn tiền";
            case CANCELLED: return "Đã hủy";
            case FORFEITED: return "Mất cọc";
            default:        return status.getValue();
        }
    }

    private void validateDateTime(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidReservationTimeException("Ngày đặt bàn phải là ngày trong tương lai");
        }

        // Lấy giờ làm việc từ ConfigService
        String hours = configService.getSiteHours();
        String[] parts = hours.split(" - ");
        LocalTime opening = LocalTime.parse(parts[0].trim());
        LocalTime closing = LocalTime.parse(parts[1].trim());

        if (time.isBefore(opening) || time.isAfter(closing)) {
            throw new InvalidReservationTimeException(
                    "Giờ đặt bàn phải trong khoảng " + opening + " - " + closing
            );
        }

        int maxAdvanceDays = configService.getReservationMaxAdvanceDays();
        LocalDate maxDate = LocalDate.now().plusDays(maxAdvanceDays);
        if (date.isAfter(maxDate)) {
            throw new InvalidReservationTimeException(
                    "Chỉ có thể đặt bàn trước tối đa " + maxAdvanceDays + " ngày"
            );
        }
    }

    private void validatePartySize(Integer partySize) {
        if (partySize < 1) {
            throw new IllegalArgumentException("Số lượng khách phải lớn hơn 0");
        }
        int maxPartySize = configService.getReservationMaxPartySize();
        if (partySize > maxPartySize) {
            throw new IllegalArgumentException("Số lượng khách tối đa là " + maxPartySize);
        }
    }

    private CoffeeTable findBestTable(List<CoffeeTable> tables, Integer partySize) {
        return tables.stream()
                .filter(table -> table.getCapacity() >= partySize)
                .min((t1, t2) -> Integer.compare(t1.getCapacity(), t2.getCapacity()))
                .orElse(tables.get(0));
    }

    private String findAlternativeTime(TableAvailabilityRequest request) {
        LocalTime currentTime = request.getReservationTime();
        LocalDate currentDate = request.getReservationDate();

        // Lấy giờ làm việc từ ConfigService
        String hours = configService.getSiteHours();
        String[] parts = hours.split(" - ");
        LocalTime opening = LocalTime.parse(parts[0].trim());
        LocalTime closing = LocalTime.parse(parts[1].trim());

        LocalTime[] alternatives = {
                currentTime.minusHours(1),
                currentTime.plusHours(1),
                currentTime.minusMinutes(30),
                currentTime.plusMinutes(30)
        };

        for (LocalTime altTime : alternatives) {
            if (altTime.isBefore(opening) || altTime.isAfter(closing)) {
                continue;
            }

            List<Reservation> activeReservations = reservationRepository.findActiveReservationsByDate(currentDate);

            Set<Integer> reservedTableIds = activeReservations.stream()
                    .filter(r -> {
                        LocalTime start = r.getReservationTime();
                        int duration = r.getDurationMinutes() != null ? r.getDurationMinutes() : 120;
                        LocalTime end = start.plusMinutes(duration);
                        return !altTime.isBefore(start) && altTime.isBefore(end);
                    })
                    .flatMap(r -> r.getTables().stream())
                    .map(CoffeeTable::getTableId)
                    .collect(Collectors.toSet());

            List<Integer> activeOrderTableIds = orderRepository.findActiveOrderTableIds();

            Set<Integer> occupiedTableIds = new HashSet<>();
            occupiedTableIds.addAll(reservedTableIds);
            occupiedTableIds.addAll(activeOrderTableIds);

            List<CoffeeTable> allTables = tableRepository.findAllActiveTables();

            long availableCount = allTables.stream()
                    .filter(table -> table.getCapacity() >= request.getPartySize())
                    .filter(table -> !occupiedTableIds.contains(table.getTableId()))
                    .count();

            if (availableCount > 0) {
                return "Gợi ý: đặt bàn lúc " + altTime + " (còn " + availableCount + " bàn)";
            }
        }

        return "Vui lòng chọn thời gian hoặc ngày khác";
    }

    private void releaseTables(Reservation reservation) {
        reservation.getTables().clear();
        reservationRepository.save(reservation);
    }

    private boolean processPayment(DepositPaymentRequest request) {
        // TODO: Tích hợp Payment Gateway thực tế
        return true;
    }

    private ReservationConfirmationResponse buildConfirmationResponse(
            Reservation reservation, CoffeeTable table, BigDecimal depositAmount, int holdMinutes) {

        String formattedDateTime = "";
        try {
            if (reservation.getReservationDate() != null && reservation.getReservationTime() != null) {
                LocalDateTime dateTime = LocalDateTime.of(
                        reservation.getReservationDate(),
                        reservation.getReservationTime()
                );
                formattedDateTime = dateTime.format(DATE_FORMATTER);
            }
        } catch (Exception e) {
            log.warn("Could not format date time: {}", e.getMessage());
        }

        return ReservationConfirmationResponse.builder()
                .success(true)
                .message("Đặt bàn thành công! Vui lòng thanh toán tiền cọc để xác nhận.")
                .reservation(convertToResponse(reservation))
                .depositAmount(depositAmount)
                .holdMinutes(holdMinutes)
                .paymentUrl("/customer/reservations/" + reservation.getReservationId() + "/deposit")
                .build();
    }

    private MakeReservationResponse convertToResponse(Reservation reservation) {
        List<Integer> tableIds = reservation.getTables().stream()
                .map(CoffeeTable::getTableId)
                .collect(Collectors.toList());

        ReservationDeposit deposit = depositRepository
                .findByReservationReservationId(reservation.getReservationId())
                .orElse(null);

        return MakeReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .customerId(reservation.getCustomer().getUserId())
                .customerName(reservation.getCustomer().getFirstName() + " " + reservation.getCustomer().getLastName())
                .customerEmail(reservation.getCustomer().getEmail())
                .customerPhone(reservation.getCustomer().getPhone())
                .partySize(reservation.getPartySize())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .durationMinutes(reservation.getDurationMinutes())
                .status(reservation.getStatus())
                .statusDisplay(getStatusDisplay(reservation.getStatus()))
                .note(reservation.getNote())
                .tableIds(tableIds)
                .depositAmount(deposit != null ? deposit.getDepositAmount() : null)
                .depositStatus(deposit != null ? deposit.getPaymentStatus().toString() : null)
                .cancellationReason(reservation.getCancellationReason())
                .cancelledAt(reservation.getCancelledAt())
                .createdAt(reservation.getCreatedAt())
                .formattedCreatedAt(reservation.getCreatedAt() != null ?
                        reservation.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null)
                .formattedReservationDateTime(reservation.getReservationDate() + " " + reservation.getReservationTime())
                .build();
    }

    private TableAvailabilityResponse.TableInfo convertToTableInfo(CoffeeTable table) {
        return TableAvailabilityResponse.TableInfo.builder()
                .tableId(table.getTableId())
                .capacity(table.getCapacity())
                .build();
    }

    private String getStatusDisplay(ReservationStatus status) {
        switch (status) {
            case PENDING: return "Chờ thanh toán";
            case CONFIRMED: return "Đã xác nhận";
            case ARRIVED: return "Đã đến";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            case NO_SHOW: return "Không đến";
            default: return status.toString();
        }
    }

    private void logSystemAction(User user, String action, String targetType, Integer targetId, String description) {
        SystemLog log = SystemLog.builder()
                .user(user)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .build();
        systemLogRepository.save(log);
    }
}