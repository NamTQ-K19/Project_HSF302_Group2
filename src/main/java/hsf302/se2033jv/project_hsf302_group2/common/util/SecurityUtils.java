// common/util/SecurityUtils.java
package hsf302.se2033jv.project_hsf302_group2.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof hsf302.se2033jv.project_hsf302_group2.auth.security.CustomUserDetails customUserDetails) {
            return customUserDetails.getUser().getUserId().longValue();
        } else if (principal instanceof hsf302.se2033jv.project_hsf302_group2.auth.security.CustomOidcUser customOidcUser) {
            if (customOidcUser.getUser() != null && customOidcUser.getUser().getUserId() != null) {
                return customOidcUser.getUser().getUserId().longValue();
            }
        } else if (principal instanceof hsf302.se2033jv.project_hsf302_group2.common.entity.User user) {
            return user.getUserId().longValue();
        }
        return null;
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof hsf302.se2033jv.project_hsf302_group2.auth.security.CustomOidcUser customOidcUser) {
            if (customOidcUser.getUser() != null) {
                return customOidcUser.getUser().getUsername();
            }
        }
        return authentication.getName();
    }
}