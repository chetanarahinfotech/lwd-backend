package com.lwd.jobportal.security;

import org.springframework.security.core.context.SecurityContextHolder;

import com.lwd.jobportal.enums.Role;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SecurityUtils {

    public static Long getUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static boolean hasRole(Role role) {
        return SecurityContextHolder
        		.getContext()
        		.getAuthentication()
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
