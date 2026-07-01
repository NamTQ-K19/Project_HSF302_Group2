package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;

public interface EmailOtpService {

    boolean verifyOtp(User user, String newEmail, String otpCode);
}
