package com.lwd.jobportal.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.auth.dto.AuthResponse;
import com.lwd.jobportal.auth.dto.RefreshTokenRequest;
import com.lwd.jobportal.auth.dto.RegisterRequest;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.AccountDisabledException;
import com.lwd.jobportal.exception.AccountLockedException;
import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.UserAlreadyExistsException;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;

    // ================= REGISTER JOB SEEKER =================
    @Transactional
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

        User savedUser = userRepository.save(user);

        emailVerificationService.createAndSendToken(savedUser);

        return savedUser;
    }

    // ================= REGISTER RECRUITER =================
    @Transactional
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

        User savedUser = userRepository.save(user);

        emailVerificationService.createAndSendToken(savedUser);

        return savedUser;
    }

    @Transactional
    public User registerCompanyAdmin(RegisterRequest request) {
        validateRegisterRequest(request);

        String email = normalizeEmail(request.getEmail());
        validateEmailNotExists(email);

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.COMPANY_ADMIN)
                .phone(request.getContactNumber().trim())
                .status(UserStatus.PENDING_APPROVAL)
                .locked(false)
                .isActive(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        emailVerificationService.createAndSendToken(savedUser);

        return savedUser;
    }
    
    
    @Transactional
    public AuthResponse login(String email,
                              String password,
                              String deviceId,
                              String deviceInfo,
                              String ipAddress) {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new BadCredentialsException("Email is required");
        }

        if (password == null || password.isBlank()) {
            throw new BadCredentialsException("Password is required");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Account checks
        if (user.isLocked()) {
            throw new AccountLockedException("Your account is locked.");
        }
        
        if (!user.isEmailVerified()) {
            throw new AccountDisabledException("Please verify your email before logging in.");
        }

        if (Boolean.FALSE.equals(user.getIsActive()) || user.getStatus() == UserStatus.SUSPENDED) {
            throw new AccountDisabledException("Your account is suspended.");
        }

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new AccountDisabledException("Your account is pending approval.");
        }
        
        if (user.getStatus() == UserStatus.COMPANY_PENDING_APPROVAL) {
            throw new AccountDisabledException("Your account is pending to company approval.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, password)
        );

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // ✅ save token per device
        refreshTokenService.createRefreshToken(
                user,
                refreshToken,
                deviceId,
                deviceInfo,
                ipAddress
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
    
    
    
    // ================= REFRESH TOKEN =================
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request,
                                     String deviceInfo,
                                     String ipAddress) {

        String rawToken = request.getRefreshToken();

        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidOperationException("Refresh token is required");
        }

        if (!jwtUtil.validateRefreshToken(rawToken)) {
            throw new InvalidOperationException("Invalid or expired refresh token");
        }

        RefreshToken storedToken = refreshTokenService.verifyStoredToken(rawToken);
        User user = storedToken.getUser();

        // ✅ get SAME deviceId
        String deviceId = storedToken.getDeviceId();

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // ✅ rotate token
        refreshTokenService.revokeToken(storedToken);

        refreshTokenService.createRefreshToken(
                user,
                newRefreshToken, // ✅ FIXED
                deviceId,        // ✅ FIXED
                deviceInfo,
                ipAddress
        );

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
    
    @Transactional
    public void logoutFromAllDevices(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTokenVersion(user.getTokenVersion() + 1); // 👈 HERE

        userRepository.save(user);
    }

    // ================= LOGOUT =================
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidOperationException("Refresh token is required");
        }

        RefreshToken storedToken = refreshTokenService.verifyStoredToken(refreshToken);
        refreshTokenService.revokeToken(storedToken);
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