package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CustomerAddress;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.AddressRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.ProfileUpdateRequest;

import java.util.List;

public interface ProfileService {

    User getCurrentUser(String username);

    void updateProfileWithoutEmailChange(User user, ProfileUpdateRequest request);

    boolean verifyCurrentPassword(User user, String rawPassword);

    void updateProfileWithNewEmail(User user, ProfileUpdateRequest request, String newEmail);

    boolean isEmailTakenByOtherUser(String email, Integer currentUserId);

    boolean isPhoneTakenByOtherUser(String phone, Integer currentUserId);

    boolean isUsernameTakenByOtherUser(String username, Integer currentUserId);

    void updateAvatar(User user, String avatarUrl);

    List<CustomerAddress> getAddresses(Integer userID);

    CustomerAddress addAddress(User user, AddressRequest request);

    CustomerAddress updateAddress(Integer addressId, Integer userId, AddressRequest request);

    void deleteAddress(Integer addressId, Integer userId);

    boolean isRecipientPhoneTaken(Integer customerId, String recipientPhone);

    boolean isRecipientPhoneTakenByOtherUser(Integer customerId, String recipientPhone, Integer addressId);
}
