package com.lwd.jobportal.security;

import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        // ❌ Not approved
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("Account is not active yet");
        }

        // ❌ Locked by admin
        if (user.isLocked()) {
            throw new org.springframework.security.authentication.LockedException(
                    "Account is locked by administrator"
            );
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .accountLocked(user.isLocked())
                .disabled(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }



}
