package hsf302.se2033jv.project_hsf302_group2.auth.service.impl;

import hsf302.se2033jv.project_hsf302_group2.auth.service.interfaces.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import hsf302.se2033jv.project_hsf302_group2.common.entity.*;
import hsf302.se2033jv.project_hsf302_group2.common.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User findByPhoneNumber(String phone) {
        return userRepository.findByPhone(phone);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User getUserById(int userId) {
        return userRepository.getUserByUserId(userId);
    }

    public void changePassword(int userId, String newPassword, String confirmPassword, String currentPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found!"));
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("The new password cannot be empty.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("The new password cannot be empty.");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("The confirm password cannot be empty.");
        }
        if (BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
            if (newPassword.equals(confirmPassword)) {
                String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
                if (!newPassword.matches(passwordPattern)) {
                    throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase letters, " +
                            "lowercase letters, numbers, and special characters!\n");
                }
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                user.setPasswordHash(hashedPassword);
                userRepository.save(user);
            } else {
                throw new IllegalArgumentException("New password does not match!");
            }
        } else {
            throw new IllegalArgumentException("Incorrect password!");
        }
    }

    public void completeGoogleAccount(int userId, String username, String phoneNumber, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        if (!"GOOGLE_OAUTH_DUMMY_HASH".equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("This account was not created with Google.");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, digits, and underscores.");
        }
        User duplicatedUsernameUser = userRepository.findByUsername(username).orElse(null);
        if (duplicatedUsernameUser != null && duplicatedUsernameUser.getUserId() != userId) {
            throw new IllegalArgumentException("Username is already in use, please choose another one.");
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("The phone number cannot be empty.");
        }
        if (!phoneNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("The phone number must contain exactly 10 digits.");
        }
        if (!phoneNumber.matches("^(0)(?!\\1{9})\\d{9}$")) {
            throw new IllegalArgumentException("Invalid phone number! It must start with 0 and not be all repeated digits.");
        }
        User duplicatedPhoneUser = userRepository.findByPhone(phoneNumber);
        if (duplicatedPhoneUser != null && duplicatedPhoneUser.getUserId() != userId) {
            throw new IllegalArgumentException("The phone number already exists, please use a different one!");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("The new password cannot be empty.");
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("The confirm password cannot be empty.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password does not match!");
        }
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!newPassword.matches(passwordPattern)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase letters, lowercase letters, numbers, and special characters!");
        }

        user.setUsername(username);
        user.setPhone(phoneNumber);
        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.save(user);
    }
}
