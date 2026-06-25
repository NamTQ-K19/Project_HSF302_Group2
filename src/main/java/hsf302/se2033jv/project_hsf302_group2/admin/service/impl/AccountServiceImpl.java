package hsf302.se2033jv.project_hsf302_group2.admin.service.impl;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.AccountFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.CreateInternalAccountRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.OtpVerificationRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.AccountResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.PageResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.AccountService;
import hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces.EmailService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Role;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.admin.repository.RoleRepository;
import hsf302.se2033jv.project_hsf302_group2.admin.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.common.util.EmailUtil;
import hsf302.se2033jv.project_hsf302_group2.common.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OtpUtil otpUtil;
    private final EmailService emailService;

    @Override
    public PageResponse<AccountResponse> getAccounts(AccountFilterRequest filterRequest, Pageable pageable) {
        log.info("Fetching accounts with filters: role={}, status={}, keyword={}",
                filterRequest.getRole(), filterRequest.getStatus(), filterRequest.getKeyword());

        Page<User> userPage = userRepository.searchUsers(
                filterRequest.getKeyword(),
                filterRequest.getRole(),
                filterRequest.getStatus(),
                pageable
        );

        Page<AccountResponse> responsePage = userPage.map(this::convertToResponse);

        return PageResponse.<AccountResponse>builder()
                .content(responsePage.getContent())
                .pageNumber(responsePage.getNumber())
                .pageSize(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }

    @Override
    public AccountResponse getAccountById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với ID: " + userId));
        return convertToResponse(user);
    }

    @Override
    @Transactional
    public void createInternalAccount(CreateInternalAccountRequest request) {
        log.info("Creating internal account for email: {}", request.getEmail());

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã tồn tại trong hệ thống");
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        // Validate phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Số điện thoại đã tồn tại trong hệ thống");
        }

        // Validate role exists
        Role role = roleRepository.findByRoleNameIgnoreCase(request.getRole())
                .orElseThrow(() -> new BusinessException("Vai trò không hợp lệ: " + request.getRole()));

        // ===== TẠO OTP =====
        String otp = otpUtil.generateOtp();

        // Sử dụng email người dùng nhập làm key để lưu OTP
        String userEmail = request.getEmail();

        // Lưu OTP với email người dùng
        otpUtil.saveOtpWithData(userEmail, otp, request);

        // ===== GỬI OTP ĐẾN EMAIL ADMIN (để admin xác nhận) =====
        String adminEmail = "luugiang205@gmail.com";
        String userName = "Luu Giang";
        String subject = "Mã OTP xác nhận tạo tài khoản - BrewMaster";
        String body = EmailUtil.getOtpContent(userName, otp);
        emailService.sendEmail(adminEmail, subject, body);

        log.info("📧 OTP sent to admin email: {}", adminEmail);
        log.info("🔐 OTP code: {}, for user email: {}", otp, userEmail);
        log.info("📝 OTP stored with key: {}", userEmail);
    }

    @Override
    @Transactional
    public void verifyOtpAndCreateAccount(OtpVerificationRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());

        // ===== XÁC THỰC OTP VỚI EMAIL NGƯỜI DÙNG =====
        if (!otpUtil.verifyOtp(request.getEmail(), request.getOtpCode())) {
            throw new BusinessException("OTP không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại.");
        }

        // Lấy dữ liệu kèm theo OTP
        CreateInternalAccountRequest userRequest = (CreateInternalAccountRequest) otpUtil.getOtpData(request.getEmail());
        if (userRequest == null) {
            throw new BusinessException("Không tìm thấy dữ liệu tài khoản. Vui lòng bắt đầu lại quá trình.");
        }

        // Kiểm tra email trong request có khớp với email đang xác thực không
        if (!userRequest.getEmail().equals(request.getEmail())) {
            throw new BusinessException("Email không khớp. Vui lòng kiểm tra lại.");
        }

        Role role = roleRepository.findByRoleNameIgnoreCase(userRequest.getRole())
                .orElseThrow(() -> new BusinessException("Vai trò không hợp lệ: " + userRequest.getRole()));

        String defaultPassword = generateDefaultPassword();

        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        user.setStatus(true);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Gửi email thông báo tạo tài khoản thành công cho người dùng
        String userName = user.getFirstName() + " " + user.getLastName();
        String subject = "Tài khoản của bạn đã được tạo - BrewMaster";
        String body = EmailUtil.getAccountCreatedContent(userName, user.getUsername(), defaultPassword);
        emailService.sendEmail(user.getEmail(), subject, body);

        // Xóa OTP khỏi cache
        otpUtil.removeOtp(request.getEmail());

        log.info("✅ Account created successfully for email: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        log.info("Resending OTP for email: {}", email);

        if (!otpUtil.hasOtp(email)) {
            throw new BusinessException("Không tìm thấy OTP. Vui lòng bắt đầu lại quá trình tạo tài khoản.");
        }

        // Lấy dữ liệu kèm theo OTP
        CreateInternalAccountRequest userRequest = (CreateInternalAccountRequest) otpUtil.getOtpData(email);
        if (userRequest == null) {
            throw new BusinessException("Không tìm thấy dữ liệu tài khoản. Vui lòng bắt đầu lại quá trình.");
        }

        // Tạo OTP mới
        String newOtp = otpUtil.generateOtp();
        otpUtil.saveOtpWithData(email, newOtp, userRequest);

        // Gửi lại OTP đến admin
        String adminEmail = "luugiang205@gmail.com";
        String userName = userRequest.getFirstName() + " " + userRequest.getLastName();
        String subject = "Mã OTP xác nhận tạo tài khoản - BrewMaster";
        String body = EmailUtil.getOtpContent(userName, newOtp);
        emailService.sendEmail(adminEmail, subject, body);

        log.info("📧 OTP resent to admin email: {}", adminEmail);
        log.info("🔐 New OTP code: {}, for user email: {}", newOtp, email);
    }

    @Override
    @Transactional
    public void lockAccount(Integer userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với ID: " + userId));

        if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new BusinessException("Không thể khóa tài khoản Admin");
        }

        if (!user.getStatus()) {
            throw new BusinessException("Tài khoản này đã bị khóa");
        }

        user.setStatus(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Gửi email thông báo khóa với lý do
        String userName = user.getFirstName() + " " + user.getLastName();
        String finalReason = reason != null && !reason.isEmpty() ? reason : "Tài khoản bị khóa do vi phạm chính sách sử dụng.";

        String subject = "Tài khoản của bạn đã bị khóa - BrewMaster";
        String body = EmailUtil.getAccountLockedContent(userName, finalReason);
        emailService.sendEmail(user.getEmail(), subject, body);

        log.info("✅ Account locked: {}, reason: {}", userId, finalReason);
    }

    @Override
    @Transactional
    public void unlockAccount(Integer userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với ID: " + userId));

        if (user.getStatus()) {
            throw new BusinessException("Tài khoản này đã được mở khóa");
        }

        user.setStatus(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String userName = user.getFirstName() + " " + user.getLastName();
        String finalReason = reason != null && !reason.isEmpty() ? reason : "Tài khoản đã được mở khóa sau khi xem xét.";

        String subject = "Tài khoản của bạn đã được mở khóa - BrewMaster";
        String body = EmailUtil.getAccountUnlockedContent(userName, finalReason);
        emailService.sendEmail(user.getEmail(), subject, body);

        log.info("✅ Account unlocked: {}, reason: {}", userId, finalReason);
    }

    @Override
    @Transactional
    public void toggleAccountStatus(Integer userId, boolean lock) {
        if (lock) {
            lockAccount(userId, null);
        } else {
            unlockAccount(userId, null);
        }
    }

    private AccountResponse convertToResponse(User user) {
        return AccountResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getRoleName() : null)
                .status(user.getStatus() ? "ACTIVE" : "LOCKED")
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String generateDefaultPassword() {
        return "Default@123";
    }
}