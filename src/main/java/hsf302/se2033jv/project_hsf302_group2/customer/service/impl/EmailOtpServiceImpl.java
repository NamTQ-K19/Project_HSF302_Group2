package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.util.OtpCacheUtil;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.EmailOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailOtpServiceImpl implements EmailOtpService {

    private final OtpCacheUtil otpCacheUtil;

    @Override
    public boolean verifyOtp(User user, String newEmail, String otpCode) {
        return otpCacheUtil.verifyOtp(user.getUserId(), newEmail, otpCode);
    }
}
