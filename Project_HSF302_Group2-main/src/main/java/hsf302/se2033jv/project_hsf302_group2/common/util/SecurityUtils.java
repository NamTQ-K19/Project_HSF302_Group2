// common/util/SecurityUtils.java
package hsf302.se2033jv.project_hsf302_group2.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        // ⭐ TẠM THỜI TRẢ VỀ ID CỐ ĐỊNH ĐỂ TEST ⭐
        // User5 = Hoàng Văn E (Customer role_id = 5)
        return 5l;

        /*
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            try {
                return Long.parseLong(((UserDetails) principal).getUsername());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
        */
    }

    public static String getCurrentUsername() {
        // ⭐ TẠM THỜI TRẢ VỀ USERNAME CỐ ĐỊNH ĐỂ TEST ⭐
        return "User5";

        /* COMMENT CODE CŨ LẠI
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
        */
    }
}