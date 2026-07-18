package hsf302.se2033jv.project_hsf302_group2.auth.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Role;
import hsf302.se2033jv.project_hsf302_group2.auth.service.interfaces.UserService;
import hsf302.se2033jv.project_hsf302_group2.common.repository.RoleRepository;
import hsf302.se2033jv.project_hsf302_group2.auth.service.interfaces.OtpService;
import hsf302.se2033jv.project_hsf302_group2.auth.service.interfaces.PasswordService;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class RegisterController {
    private OtpService otpService;
    private PasswordService passwordService;
    private final UserService userService;
    private final RoleRepository roleRepository;

    public RegisterController(OtpService otpService, PasswordService passwordService, UserService userService, RoleRepository roleRepository) {
        this.otpService = otpService;
        this.passwordService = passwordService;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping(path = "/register")
    public String showRegister(Model model){
        model.addAttribute("user", new User());
        return "account/register";
    }

    @PostMapping(path = "/register")
    public String doRegister(Model model, @ModelAttribute User user,
                             @RequestParam(required = false) String confirmPassword,   // ← THÊM
                             HttpSession session){
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String username = user.getUsername();
        String phoneNumber = user.getPhone();
        String email = user.getEmail();
        String password = user.getPasswordHash();

        if (firstName == null || firstName.isBlank()) {
            model.addAttribute("errorMessage", "First name cannot be null or blank");
            return "account/register";
        }
        if (lastName == null || lastName.isBlank()) {
            model.addAttribute("errorMessage", "Last name cannot be null or blank");
            return "account/register";
        }
        if (!firstName.matches("^[\\p{L} ]+$") || !lastName.matches("^[\\p{L} ]+$")) {
            model.addAttribute("errorMessage", "Name can only contain letters");
            return "account/register";
        }

        if (username == null || username.isBlank()) {
            model.addAttribute("errorMessage", "Username cannot be null or blank");
            return "account/register";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            model.addAttribute("errorMessage", "Username can only contain letters, digits, and underscores");
            return "account/register";
        }
        if (userService.findByUsername(username) != null) {
            model.addAttribute("errorMessage", "Username is already in use");
            return "account/register";
        }

        if (email == null || email.isBlank()) {
            model.addAttribute("errorMessage", "Email cannot be null or blank");
            return "account/register";
        }
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            model.addAttribute("errorMessage", "Invalid email format");
            return "account/register";
        }
        if (userService.findByEmail(email) != null) {
            model.addAttribute("errorMessage", "Email is already registered");
            return "account/register";
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            model.addAttribute("errorMessage", "Phone number cannot be null or blank");
            return "account/register";
        }
        if (!phoneNumber.matches("\\d{10}")) {
            model.addAttribute("errorMessage", "Phone number must contain exactly 10 digits");
            return "account/register";
        }
        if (phoneNumber.equals("0000000000")) {
            model.addAttribute("errorMessage", "Phone number cannot be all zeros");
            return "account/register";
        }
        if (userService.findByPhoneNumber(phoneNumber) != null) {
            model.addAttribute("errorMessage", "Phone number is already in use");
            return "account/register";
        }

        // ← THÊM: Validate độ phức tạp mật khẩu theo đúng Business Rule đã tài liệu hóa
        if (password == null || password.isBlank()) {
            model.addAttribute("errorMessage", "Password cannot be null or blank");
            return "account/register";
        }
        if (password.contains(" ")) {
            model.addAttribute("errorMessage", "Password cannot contain spaces");
            return "account/register";
        }
        if (password.length() < 8 || password.length() > 64) {
            model.addAttribute("errorMessage", "Password must be between 8 and 64 characters");
            return "account/register";
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s]).+$")) {
            model.addAttribute("errorMessage",
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
            return "account/register";
        }

        // ← THÊM: Validate confirm password ở Backend
        if (confirmPassword == null || confirmPassword.isBlank()) {
            model.addAttribute("errorMessage", "Confirm password cannot be null or blank");
            return "account/register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Password and confirm password do not match");
            return "account/register";
        }

        // All validations passed. Hash the password and set default fields.
        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPasswordHash(hashPassword);
        user.setAvatarUrl("avatar.jpeg");
        user.setStatus(true);

        Role customerRole = roleRepository.findByRoleName("CUSTOMER");
        if (customerRole == null) {
            Role r = new Role();
            r.setRoleName("CUSTOMER");
            customerRole = roleRepository.save(r);
        }
        user.setRole(customerRole);

        session.setAttribute("pendingUser", user);
        session.setAttribute("registerOtpAttempts", 0);   // Gap A: khởi tạo bộ đếm số lần nhập sai OTP
        passwordService.sendOtpForRegister(user.getEmail());

        return "redirect:/verify-email";
    }

    @GetMapping("/verify-email")
    public String showVerifyOtp(HttpSession session, Model model) {
        User pendingUser = (User) session.getAttribute("pendingUser");

        if (pendingUser == null) {
            return "redirect:/register?error=expired";
        }

        Integer attempts = (Integer) session.getAttribute("registerOtpAttempts");
        if (attempts != null && attempts > 0) {
            model.addAttribute("attemptsLeft", 3 - attempts);
        }

        model.addAttribute("email", pendingUser.getEmail());
        return "account/verify-email";
    }

    @PostMapping("/verify-email")
    public String verifyOtp(String otp,
                            Model model,
                            HttpSession session) {

        User pendingUser = (User) session.getAttribute("pendingUser");

        if (pendingUser == null) {
            model.addAttribute("errorMessage", "Session has expired!");
            return "account/register";
        }

        Integer attempts = (Integer) session.getAttribute("registerOtpAttempts");
        if (attempts == null) {
            attempts = 0;
        }

        // Gap A: đã vượt quá số lần cho phép trước đó (phòng trường hợp F5/gửi lại form cũ)
        if (attempts >= 3) {
            session.removeAttribute("pendingUser");
            session.removeAttribute("registerOtpAttempts");
            String errorMessage = "You have exceeded the maximum number of attempts (3). Please register again.";
            return "redirect:/register?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        }

        boolean valid = otpService.validateOtp(pendingUser.getEmail(), otp);

        if (!valid) {
            attempts++;
            session.setAttribute("registerOtpAttempts", attempts);

            // Gap A: vừa chạm mốc 3 lần sai -> buộc đăng ký lại từ đầu
            if (attempts >= 3) {
                session.removeAttribute("pendingUser");
                session.removeAttribute("registerOtpAttempts");
                String errorMessage = "You have exceeded the maximum number of attempts (3). Please register again.";
                return "redirect:/register?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
            }

            // Gap B: quay lại ĐÚNG trang verify-email để nhập lại ngay, thay vì /login
            model.addAttribute("errorMessage", "The OTP code is incorrect or has expired!");
            model.addAttribute("attemptsLeft", 3 - attempts);
            model.addAttribute("email", pendingUser.getEmail());
            return "account/verify-email";
        }

        try {
            userService.save(pendingUser);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", pendingUser.getEmail());
            model.addAttribute("otp", otp);
            return "account/verify-email";
        }

        session.removeAttribute("pendingUser");
        session.removeAttribute("registerOtpAttempts");

        return "redirect:/login?successMessage=Your account has been successfully registered. Welcome aboard!";
    }
}