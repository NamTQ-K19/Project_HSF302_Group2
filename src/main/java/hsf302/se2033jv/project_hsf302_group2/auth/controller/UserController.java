package hsf302.se2033jv.project_hsf302_group2.auth.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.auth.security.LoggedUser;
import hsf302.se2033jv.project_hsf302_group2.auth.service.interfaces.UserService;

@Controller
@RequestMapping(value = "/profile")
public class UserController {

    private final UserService userService;
    private final LoggedUser loggedUser;
    private final UserRepository userRepository;

    public UserController(UserService userService, LoggedUser loggedUser, UserRepository userRepository) {
        this.userService = userService;
        this.loggedUser = loggedUser;
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "")
    public String viewProfile(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        User sessionUser = loggedUser.getLoggedCustomer();
        if (sessionUser == null) {
            return "redirect:/login";
        }
        User user = userService.getUserById(sessionUser.getUserId());
        model.addAttribute("user", user);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfile(Model model, HttpSession httpSession) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String reminder = (String) httpSession.getAttribute("profileReminder");
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        User sessionUser = loggedUser.getLoggedCustomer();
        if (sessionUser == null) {
            return "redirect:/login";
        }

        if (reminder != null) {
            model.addAttribute("reminder", reminder);
            httpSession.removeAttribute("profileReminder");
        }
        User user = userService.getUserById(sessionUser.getUserId());
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String editProfile(@ModelAttribute(name = "user") User user, @RequestParam(value = "imgFile", required = false) MultipartFile imgFile, Model model) {
        User sessionUser = null;
        try {
            sessionUser = loggedUser.getLoggedCustomer();
            if (user.getUserId() != sessionUser.getUserId()) {
                model.addAttribute("title", "Security Error");
                model.addAttribute("errorMessage", "Update request denied due to invalid data.");
                return "error-page";
            }
            if (user.getPhone() == null || user.getPhone().isEmpty()) {
                return "redirect:/profile/edit";
            }
            userService.updateUser(user, imgFile);

            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", sessionUser);
            return "profile/edit";
        }
    }

    @GetMapping("/complete-google-account")
    public String completeGoogleAccount(Model model, HttpSession httpSession) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }

        User sessionUser = loggedUser.getLoggedCustomer();
        if (sessionUser == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(sessionUser.getUserId());
        if (!"GOOGLE_OAUTH_DUMMY_HASH".equals(user.getPasswordHash())) {
            return "redirect:/profile";
        }

        String reminder = (String) httpSession.getAttribute("profileReminder");
        if (reminder != null) {
            model.addAttribute("reminder", reminder);
            httpSession.removeAttribute("profileReminder");
        }

        model.addAttribute("user", user);
        return "profile/complete-google-account";
    }

    @PostMapping("/complete-google-account")
    public String completeGoogleAccount(@RequestParam("userId") int userId,
                                        @RequestParam("username") String username,
                                        @RequestParam("phoneNumber") String phoneNumber,
                                        @RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        Model model) {
        try {
            User sessionUser = loggedUser.getLoggedCustomer();
            if (sessionUser == null || sessionUser.getUserId() != userId) {
                model.addAttribute("title", "Security Error");
                model.addAttribute("errorMessage", "Update request denied due to invalid data.");
                return "error-page";
            }

            userService.completeGoogleAccount(userId, username, phoneNumber, newPassword, confirmPassword);
            return "redirect:/home";
        } catch (IllegalArgumentException e) {
            User user = userService.getUserById(userId);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", user);
            return "profile/complete-google-account";
        }
    }

    @RequestMapping(value = "/changePassword")
    public String changePassword(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        User user = loggedUser.getLoggedCustomer();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile/changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(@ModelAttribute(name = "user") User user, @RequestParam(value = "currentPassword") String currentPassword, @RequestParam(value = "newPassword") String newPassword, @RequestParam(value = "confirmPassword") String confirmPassword, Model model) {
        try {
            userService.changePassword(user.getUserId(), newPassword, confirmPassword, currentPassword);
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", user);
            return "profile/changePassword";
        }
    }
}
