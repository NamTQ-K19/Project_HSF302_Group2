package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.util.AvatarStorageUtil;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.AddressRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.OtpVerifyRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.PasswordVerifyRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.ProfileUpdateRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.EmailOtpService;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final EmailOtpService emailOtpService;
    private final AvatarStorageUtil avatarStorageUtil;
    private final UserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @GetMapping
    public String showProfile(Model model, Authentication auth){
        User user = profileService.getCurrentUser(auth.getName());

        ProfileUpdateRequest form = new ProfileUpdateRequest();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setPhone(user.getPhone());
        form.setEmail(user.getEmail());
        form.setUsername(user.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("profileForm", form);
        model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
        model.addAttribute("addressForm", new AddressRequest());

        return "customer/profile/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileUpdateRequest request,
            BindingResult bindingResult,
            Authentication auth,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = profileService.getCurrentUser(auth.getName());

        // 1. Kiểm tra Bean Validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profileForm", request);
            model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
            model.addAttribute("addressForm", new AddressRequest());
            return "customer/profile/profile";
        }

        // 2. Kiểm tra phone trùng
        if (profileService.isPhoneTakenByOtherUser(request.getPhone(), user.getUserId())) {
            bindingResult.rejectValue("phone", "duplicate", "Số điện thoại này đã được sử dụng bởi tài khoản khác.");
            model.addAttribute("user", user);
            model.addAttribute("profileForm", request);
            model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
            model.addAttribute("addressForm", new AddressRequest());
            return "customer/profile/profile";
        }

        // 3. Kiểm tra username trùng
        if (profileService.isUsernameTakenByOtherUser(request.getUsername(), user.getUserId())) {
            bindingResult.rejectValue("username", "duplicate", "Tên người dùng này đã tồn tại.");
            model.addAttribute("user", user);
            model.addAttribute("profileForm", request);
            model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
            model.addAttribute("addressForm", new AddressRequest());
            return "customer/profile/profile";
        }

        // 4. Kiểm tra email trùng khi thay đổi
        if (!request.getEmail().equalsIgnoreCase(user.getEmail()) && profileService.isEmailTakenByOtherUser(request.getEmail(), user.getUserId())) {
            bindingResult.rejectValue("email", "duplicate", "Email này đã được sử dụng bởi tài khoản khác.");
            model.addAttribute("user", user);
            model.addAttribute("profileForm", request);
            model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
            model.addAttribute("addressForm", new AddressRequest());
            return "customer/profile/profile";
        }

        boolean emailChanged = !request.getEmail().equalsIgnoreCase(user.getEmail());

        if (!emailChanged) {
            String oldUsername = user.getUsername(); // Lấy username cũ

            // 1. LƯU XUỐNG DATABASE TRƯỚC
            profileService.updateProfileWithoutEmailChange(user, request);

            // 2. CẬP NHẬT SESSION SAU KHI DB ĐÃ LƯU THÀNH CÔNG
            if (!oldUsername.equals(request.getUsername())) {
                updateSecurityContext(request.getUsername(), auth, httpRequest, httpResponse);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
            return "redirect:/customer/profile";
        }

        // --- NẾU CÓ THAY ĐỔI EMAIL ---
        // Truyền thêm biến username để không bị mất khi chuyển sang trang verify password
        model.addAttribute("user", user);
        model.addAttribute("newEmail", request.getEmail());
        model.addAttribute("username", request.getUsername());
        model.addAttribute("profileData", request);
        model.addAttribute("passwordForm", new PasswordVerifyRequest());

        return "customer/profile/profile-verify-password";
    }

    @PostMapping("/verify-password")
    public String verifyPassword(
            @Valid @ModelAttribute("passwordForm") PasswordVerifyRequest request,
            BindingResult bindingResult,
            @RequestParam String newEmail,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            @RequestParam String username,
            Authentication auth,
            Model model){

        // Lấy user hiện tại
        User user = profileService.getCurrentUser(auth.getName());

        // Kiểm tra validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("newEmail", newEmail);
            model.addAttribute("profileData", buildProfileData(firstName, lastName, phone, newEmail));
            model.addAttribute("username", username);
            return "customer/profile/profile-verify-password";
        }

        // Kiểm tra mật khẩu
        if (!profileService.verifyCurrentPassword(user, request.getCurrentPassword())) {
            model.addAttribute("errorMessage", "Mật khẩu không chính xác. Vui lòng thử lại.");
            model.addAttribute("user", user);
            model.addAttribute("newEmail", newEmail);
            model.addAttribute("profileData", buildProfileData(firstName, lastName, phone, newEmail));
            model.addAttribute("username", username);
            model.addAttribute("passwordForm", request);
            return "customer/profile/profile-verify-password";
        }

        emailOtpService.generateAndSendOtp(user, newEmail);

        model.addAttribute("user", user);
        model.addAttribute("newEmail", newEmail);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("phone", phone);
        model.addAttribute("username", username);
        model.addAttribute("otpForm", new OtpVerifyRequest());
        model.addAttribute("successMessage", "Mã OTP đã được gửi đến " + newEmail + ". Có hiệu lực trong 5 phút.");

        return "customer/profile/profile-verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @Valid @ModelAttribute("otpForm") OtpVerifyRequest request,
            BindingResult bindingResult,
            @RequestParam String newEmail,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            @RequestParam String username,
            Authentication auth,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            Model model,
            RedirectAttributes redirectAttributes){

        User user = profileService.getCurrentUser(auth.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("newEmail", newEmail);
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("phone", phone);
            model.addAttribute("username", username);
            model.addAttribute("otpForm", request);
            return "customer/profile/profile-verify-otp";
        }

        boolean otpValid = emailOtpService.verifyOtp(user, newEmail, request.getOtpCode());
        if (!otpValid) {
            model.addAttribute("user", user);
            model.addAttribute("newEmail", newEmail);
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("phone", phone);
            model.addAttribute("username", username);
            model.addAttribute("otpForm", request);
            model.addAttribute("errorMessage", "Mã OTP không hợp lệ hoặc đã hết hạn. Vui lòng thử lại hoặc yêu cầu gửi lại mã.");
            return "customer/profile/profile-verify-otp";
        }

        // Đóng gói dữ liệu để lưu
        ProfileUpdateRequest profileData = new ProfileUpdateRequest();
        profileData.setFirstName(firstName);
        profileData.setLastName(lastName);
        profileData.setPhone(phone);
        profileData.setEmail(newEmail);
        profileData.setUsername(username);

        String oldUsername = user.getUsername();

        // 1. LƯU XUỐNG DB TRƯỚC
        profileService.updateProfileWithNewEmail(user, profileData, newEmail);

        // 2. CẬP NHẬT SESSION NẾU USERNAME THAY ĐỔI
        if (!oldUsername.equals(username)) {
            updateSecurityContext(username, auth, httpRequest, httpResponse);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/customer/profile";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(
            @RequestParam String newEmail,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            @RequestParam String username,
            Authentication auth,
            Model model){

        User user = profileService.getCurrentUser(auth.getName());

        emailOtpService.generateAndSendOtp(user, newEmail);

        model.addAttribute("user", user);
        model.addAttribute("newEmail", newEmail);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("phone", phone);
        model.addAttribute("otpForm", new OtpVerifyRequest());
        model.addAttribute("username", username);
        model.addAttribute("successMessage",
                "Mã OTP đã được gửi lại đến " + newEmail + ". Có hiệu lực trong 5 phút.");
        return "customer/profile/profile-verify-otp";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = profileService.getCurrentUser(auth.getName());

        try {
            if (user.getAvatarUrl() != null) {
                avatarStorageUtil.delete(user.getAvatarUrl());
            }

            String avatarUrl = avatarStorageUtil.store(avatarFile);
            profileService.updateAvatar(user, avatarUrl);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật ảnh đại diện thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Cập nhật ảnh đại diện thất bại: " + e.getMessage());
        }

        return "redirect:/customer/profile";
    }

    @PostMapping("/address/add")
    public String addAddress(
            @Valid @ModelAttribute("addressForm") AddressRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = profileService.getCurrentUser(auth.getName());

        if (bindingResult.hasErrors()) {
            ProfileUpdateRequest form = new ProfileUpdateRequest();
            form.setFirstName(user.getFirstName());
            form.setLastName(user.getLastName());
            form.setPhone(user.getPhone());
            form.setEmail(user.getEmail());
            form.setUsername(user.getUsername());

            model.addAttribute("user", user);
            model.addAttribute("profileForm", new ProfileUpdateRequest());
            model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
            model.addAttribute("addressForm", request);
            model.addAttribute("showAddressForm", true);
            return "customer/profile/profile";
        }

        if (profileService.isRecipientPhoneTaken(user.getUserId(), request.getRecipientPhone())) {
            bindingResult.rejectValue("recipientPhone", "error.recipientPhone", "Đã có địa chỉ sử dụng số điện thoại này.");

            model.addAttribute("user", user);
            model.addAttribute("profileForm", new ProfileUpdateRequest());
            model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
            model.addAttribute("addressForm", request);
            model.addAttribute("showAddressForm", true);
            return "customer/profile/profile";
        }

        profileService.addAddress(user, request);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ thành công!");
        return "redirect:/customer/profile";
    }

    @PostMapping("/address/{addressId}/edit")
    public String editAddress(
            @PathVariable Integer addressId,
            @Valid @ModelAttribute("editAddressForm") AddressRequest request,
            BindingResult bindingResult,
            Authentication auth,
            RedirectAttributes redirectAttrs,
            Model model) {

        User user = profileService.getCurrentUser(auth.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profileForm", new ProfileUpdateRequest());
            model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
            model.addAttribute("addressForm", new AddressRequest());

            // Prepare model to re-open the Edit Address modal with submitted values and show errors there
            model.addAttribute("openEditModal", true);
            model.addAttribute("editAddressId", addressId);
            model.addAttribute("editLabel", request.getLabel());
            model.addAttribute("editFullAddress", request.getFullAddress());
            model.addAttribute("editRecipientName", request.getRecipientName());
            model.addAttribute("editRecipientPhone", request.getRecipientPhone());
            model.addAttribute("editAddressForm", request);
            model.addAttribute("editHasValidationErrors", true);
            return "customer/profile/profile";
        }

        if (profileService.isRecipientPhoneTakenByOtherUser(user.getUserId(), request.getRecipientPhone(), addressId)) {
            bindingResult.rejectValue("recipientPhone", "error.recipientPhone", "Đã có địa chỉ sử dụng số điện thoại này.");

            // Prepare model to re-open the Edit Address modal with submitted values and show errors there
            model.addAttribute("user", user);
            model.addAttribute("profileForm", new ProfileUpdateRequest());
            model.addAttribute("addresses", profileService.getAddresses(user.getUserId()));
            model.addAttribute("addressForm", new AddressRequest());

            // Provide the edit modal fields so template can pre-fill them
            model.addAttribute("openEditModal", true);
            model.addAttribute("editAddressId", addressId);
            model.addAttribute("editLabel", request.getLabel());
            model.addAttribute("editFullAddress", request.getFullAddress());
            model.addAttribute("editRecipientName", request.getRecipientName());
            model.addAttribute("editRecipientPhone", request.getRecipientPhone());

            // Also include the BindingResult errors for the address form under the key 'editAddressForm'
            model.addAttribute("editAddressForm", request);
            model.addAttribute("editPhoneError", "Đã có địa chỉ sử dụng số điện thoại này.");
            return "customer/profile/profile";
        }

        try {
            profileService.updateAddress(addressId, user.getUserId(), request);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Cập nhật địa chỉ thành công.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage",
                    "Không tìm thấy địa chỉ hoặc không có quyền truy cập.");
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/address/{addressId}/delete")
    public String deleteAddress(
            @PathVariable Integer addressId,
            Authentication auth,
            RedirectAttributes redirectAttrs) {

        User user = profileService.getCurrentUser(auth.getName());
        try {
            profileService.deleteAddress(addressId, user.getUserId());
            redirectAttrs.addFlashAttribute("successMessage",
                    "Xóa địa chỉ thành công.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage",
                    "Không tìm thấy địa chỉ hoặc không có quyền truy cập.");
        }
        return "redirect:/customer/profile";
    }

    private ProfileUpdateRequest buildProfileData(String firstName, String lastName,
                                                  String phone, String email){
        ProfileUpdateRequest form = new ProfileUpdateRequest();
        form.setFirstName(firstName);
        form.setLastName(lastName);
        form.setPhone(phone);
        form.setEmail(email);
        return form;
    }

    private void updateSecurityContext(String newUsername, Authentication currentAuth, HttpServletRequest request, HttpServletResponse response) {
        // 1. Load lại UserDetails mới từ DB bằng username mới
        UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(newUsername);

        // 2. Tạo một Token Authentication mới
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                currentAuth.getCredentials(),
                updatedUserDetails.getAuthorities()
        );

        // 3. Set vào SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(newAuth);
        SecurityContextHolder.setContext(context);

        // 4. LƯU Ý QUAN TRỌNG CHO SPRING SECURITY 6: Phải lưu lại vào Repository thì Session mới nhận
        securityContextRepository.saveContext(context, request, response);
    }
}
