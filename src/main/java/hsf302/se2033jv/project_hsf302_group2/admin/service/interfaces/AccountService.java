package hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.AccountFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.CreateInternalAccountRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.OtpVerificationRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.AccountResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AccountService {

    PageResponse<AccountResponse> getAccounts(AccountFilterRequest filterRequest, Pageable pageable);

    AccountResponse getAccountById(Integer userId);

    void createInternalAccount(CreateInternalAccountRequest request);

    void verifyOtpAndCreateAccount(OtpVerificationRequest request);

    void resendOtp(String email);

    void lockAccount(Integer userId, String reason);

    void unlockAccount(Integer userId, String reason);

    void toggleAccountStatus(Integer userId, boolean lock);
}