package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CustomerAddress;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.AddressRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.ProfileUpdateRequest;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CustomerAddressRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.ProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerAddressRepository customerAddressRepository;

    @Override
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public void updateProfileWithoutEmailChange(User user, ProfileUpdateRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setUsername(request.getUsername());

        userRepository.save(user);
    }

    @Override
    public boolean verifyCurrentPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    @Override
    @Transactional
    public void updateProfileWithNewEmail(User user, ProfileUpdateRequest request, String newEmail) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setEmail(newEmail);
        user.setUsername(request.getUsername());

        userRepository.save(user);
    }

    @Override
    public boolean isEmailTakenByOtherUser(String email, Integer currentUserId) {
        return userRepository.existsByEmailAndUserIdNot(email, currentUserId);
    }

    @Override
    public boolean isPhoneTakenByOtherUser(String phone, Integer currentUserId) {
        return userRepository.existsByPhoneAndUserIdNot(phone, currentUserId);
    }

    @Override
    public boolean isUsernameTakenByOtherUser(String username, Integer currentUserId) {
        return userRepository.existsByUsernameAndUserIdNot(username, currentUserId);
    }

    @Override
    @Transactional
    public void updateAvatar(User user, String avatarUrl) {
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    @Override
    public List<CustomerAddress> getAddresses(Integer userID) {
        return customerAddressRepository.findByCustomerUserIdOrderByCreatedAtDesc(userID);
    }

    @Override
    @Transactional
    public CustomerAddress addAddress(User user, AddressRequest request) {
        CustomerAddress address = CustomerAddress.builder()
                .customer(user)
                .label(request.getLabel() == "" ? null : request.getLabel())
                .fullAddress(request.getFullAddress())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .build();

        return customerAddressRepository.save(address);
    }

    @Override
    @Transactional
    public CustomerAddress updateAddress(Integer addressId, Integer userId, AddressRequest request) {
        CustomerAddress customerAddress = customerAddressRepository
                .findByAddressIdAndCustomerUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        customerAddress.setLabel(request.getLabel() == "" ? null : request.getLabel());
        customerAddress.setFullAddress(request.getFullAddress());
        customerAddress.setRecipientName(request.getRecipientName());
        customerAddress.setRecipientPhone(request.getRecipientPhone());

        return customerAddressRepository.save(customerAddress);
    }

    @Override
    public void deleteAddress(Integer addressId, Integer userId) {
        CustomerAddress customerAddress = customerAddressRepository
                .findByAddressIdAndCustomerUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        customerAddressRepository.delete(customerAddress);
    }

    @Override
    public boolean isRecipientPhoneTaken(Integer customerId, String recipientPhone) {
        return customerAddressRepository.existsByCustomer_UserIdAndRecipientPhone(customerId, recipientPhone);
    }


    @Override
    public boolean isRecipientPhoneTakenByOtherUser(Integer customerId, String recipientPhone, Integer addressId) {
        return customerAddressRepository.existsByCustomer_UserIdAndRecipientPhoneAndAddressIdNot(customerId, recipientPhone, addressId);
    }
}
