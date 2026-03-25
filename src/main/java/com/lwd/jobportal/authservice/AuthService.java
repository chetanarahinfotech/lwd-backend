package com.lwd.jobportal.authservice;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.authdto.RegisterRequest;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.AccountDisabledException;
import com.lwd.jobportal.exception.AccountLockedException;
import com.lwd.jobportal.exception.UserAlreadyExistsException;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // ================= REGISTER JOB SEEKER =================
    public User registerJobSeeker(RegisterRequest request) {
        validateRegisterRequest(request);

        String email = normalizeEmail(request.getEmail());

        validateEmailNotExists(email);

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.JOB_SEEKER)
                .phone(request.getContactNumber().trim())
                .status(UserStatus.ACTIVE)
                .locked(false)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    // ================= REGISTER RECRUITER =================
    public User registerRecruiter(RegisterRequest request) {
        validateRegisterRequest(request);

        String email = normalizeEmail(request.getEmail());

        validateEmailNotExists(email);

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.RECRUITER)
                .phone(request.getContactNumber().trim())
                .status(UserStatus.PENDING_APPROVAL)
                .locked(false)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    // ================= LOGIN =================
    public String login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new BadCredentialsException("Email is required");
        }

        if (password == null || password.isBlank()) {
            throw new BadCredentialsException("Password is required");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, password)
            );

        } catch (LockedException e) {
            throw new AccountLockedException("Your account is locked. Contact administrator.");
        } catch (DisabledException e) {
            throw new AccountDisabledException("Your account is not active.");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password")
                );

        return jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ================= HELPER METHODS =================
    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getContactNumber() == null || request.getContactNumber().trim().isBlank()) {
            throw new IllegalArgumentException("Contact number is required");
        }
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already registered");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}