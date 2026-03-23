package com.lwd.jobportal.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lwd.jobportal.dto.authdto.UserPrincipal;
import com.lwd.jobportal.enums.Role;

public class SecurityUtils {

    // 🔐 Get Authentication safely
    private static Authentication getAuth() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            throw new RuntimeException("User not authenticated");
        }

        return auth;
    }

    // 🔥 Get UserId safely
    public static Long getUserId() {

        Object principal = getAuth().getPrincipal();

        // ✅ Case 1: Proper UserPrincipal
        if (principal instanceof UserPrincipal user) {
            return user.getUserId();
        }

        // ✅ Case 2: Direct Long (fallback)
        if (principal instanceof Long userId) {
            return userId;
        }

        // ✅ Case 3: String (JWT subject)
        if (principal instanceof String str) {
            return Long.parseLong(str);
        }

     // ✅ Case 3: Numeric String only
        if (principal instanceof String str) {
            if ("anonymousUser".equals(str)) {
                throw new RuntimeException("User not authenticated");
            }

            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid principal. Expected userId but got: " + str);
            }
        }
        throw new RuntimeException("Invalid principal type: " + principal);
    }


    // 🔥 Role check (robust)
    public static boolean hasRole(Role role) {

        return getAuth().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_" + role.name()));
    }

    // 🔥 Get primary role
    public static Role getRole() {

        return getAuth().getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .map(Role::valueOf)
                .findFirst()
                .orElseThrow(() -> 
                        new IllegalStateException("Role not found"));
    }
}
