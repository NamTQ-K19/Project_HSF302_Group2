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
    public String doRegister(Model model, @ModelAttribute User user, HttpSession session){
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
        passwordService.sendOtpForRegister(user.getEmail());

        return "redirect:/verify-email";
    }

    @GetMapping("/verify-email")
    public String showVerifyOtp(HttpSession session, Model model) {
        User pendingUser = (User) session.getAttribute("pendingUser");

        if (pendingUser == null) {
            return "redirect:/register?error=expired";
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

        boolean valid = otpService.validateOtp(pendingUser.getEmail(), otp);

        if (!valid) {
            return "redirect:/login?errorMessage=The OTP code is incorrect or has expired!";
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

        return "redirect:/login?successMessage=Your account has been successfully registered. Welcome aboard!";
    }
}