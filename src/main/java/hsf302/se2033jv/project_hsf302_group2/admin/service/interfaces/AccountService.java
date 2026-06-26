package hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.AccountFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.CreateInternalAccountRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.OtpVerificationRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.AccountResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.PageResponse;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface AccountService {

    PageResponse<AccountResponse> getAccounts(AccountFilterRequest filterRequest, Pageable pageable);

    AccountResponse getAccountById(Integer userId);

    void createInternalAccount(CreateInternalAccountRequest request);

    void verifyOtpAndCreateAccount(OtpVerificationRequest request);

    void resendOtp(String email);

    void lockAccount(Integer userId, String reason);

    void unlockAccount(Integer userId, String reason);

    void toggleAccountStatus(Integer userId, boolean lock);

    AccountResponse getAccountByUsername(String userName);

    void updateProfile(String username, String firstName, String lastName, String phone);

    void changePassword(String username, String currentPassword, String newPassword);

    void updateAvatar(String username, String fileName);
}