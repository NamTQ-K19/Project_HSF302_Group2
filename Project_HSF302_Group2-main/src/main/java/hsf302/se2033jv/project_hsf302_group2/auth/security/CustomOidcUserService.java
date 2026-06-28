package hsf302.se2033jv.project_hsf302_group2.auth.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import hsf302.se2033jv.project_hsf302_group2.common.entity.*;
import hsf302.se2033jv.project_hsf302_group2.common.repository.UserRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.RoleRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOidcUserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String googleId = oidcUser.getAttribute("sub");
        String email = oidcUser.getEmail();
        String givenName = oidcUser.getGivenName();
        String familyName = oidcUser.getFamilyName();
        String picture = oidcUser.getAttribute("picture");

        if (givenName == null) givenName = "Google";
        if (familyName == null) familyName = "User";

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setAvatarUrl(picture);
        } else {
            user = new User();
            user.setUsername(googleId);
            user.setEmail(email);
            user.setFirstName(givenName);
            user.setLastName(familyName);
            user.setAvatarUrl(picture);
            user.setPhone(""); // Phone must be empty to trigger profile completion
            user.setPasswordHash("GOOGLE_OAUTH_DUMMY_HASH");
            user.setStatus(true);

            Role customerRole = roleRepository.findById(5).orElseGet(() -> {
                Role r = new Role();
                r.setRoleId(5);
                r.setRoleName("Customer");
                return roleRepository.save(r);
            });
            user.setRole(customerRole);
        }

        userRepository.save(user);

        // FIX: Trả về OidcUser wrapper có getName() = username trong DB
        // DefaultOidcUser.getName() mặc định trả về Google "sub" ID → ProfileController
        // gọi auth.getName() lấy được sub ID, sau đó tìm user theo username không thấy
        // → RuntimeException("User not found")
        // Wrapper này override getName() để trả về user.getUsername() thực trong DB
        final User finalUser = user;
        final OidcUser finalOidcUser = oidcUser;
        final Set<SimpleGrantedAuthority> mappedAuthorities = Set.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

        return new OidcUser() {
            @Override public String getName() { return finalUser.getUsername(); }
            @Override public Map<String, Object> getAttributes() { return finalOidcUser.getAttributes(); }
            @Override public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() { return mappedAuthorities; }
            @Override public OidcIdToken getIdToken() { return finalOidcUser.getIdToken(); }
            @Override public OidcUserInfo getUserInfo() { return finalOidcUser.getUserInfo(); }
            @Override public Map<String, Object> getClaims() { return finalOidcUser.getClaims(); }
        };
    }
}
