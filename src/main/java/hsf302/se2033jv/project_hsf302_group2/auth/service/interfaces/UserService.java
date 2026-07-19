package hsf302.se2033jv.project_hsf302_group2.auth.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService {
    User findByEmail(String email);
    User findByPhoneNumber(String phone);
    User findByUsername(String username);
    User save(User user);
    User getUserById(int userId);
    void changePassword(int userId, String newPassword, String confirmPassword, String currentPassword);
    void completeGoogleAccount(int userId, String username, String phoneNumber, String newPassword, String confirmPassword);
}