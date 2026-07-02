package hsf302.se2033jv.project_hsf302_group2.auth.security;

import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CustomOidcUser implements OidcUser {
    private final OidcUser delegate;
    private final User user;
    private final Set<? extends GrantedAuthority> authorities;

    public CustomOidcUser(OidcUser delegate, User user, Set<? extends GrantedAuthority> authorities) {
        this.delegate = delegate;
        this.user = user;
        this.authorities = authorities;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return user != null ? user.getUsername() : (delegate != null ? delegate.getName() : null);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate != null ? delegate.getAttributes() : Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : Set.of();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate != null ? delegate.getIdToken() : null;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate != null ? delegate.getUserInfo() : null;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate != null ? delegate.getClaims() : Map.of();
    }
}
