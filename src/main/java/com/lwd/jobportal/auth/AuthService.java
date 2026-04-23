package com.lwd.jobportal.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
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

        return userRepository.save(user);
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

        return userRepository.save(user);
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
    public AuthResponse login(String email, String password, String deviceInfo, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new BadCredentialsException("Email is required");
        }

        if (password == null || password.isBlank()) {
            throw new BadCredentialsException("Password is required");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // ✅ Business/account state checks first
        if (user.isLocked()) {
            throw new AccountLockedException("Your account is locked. Contact administrator.");
        }

        if (Boolean.FALSE.equals(user.getIsActive()) || user.getStatus() == UserStatus.SUSPENDED) {
            throw new AccountDisabledException("Your account is suspended. Contact administrator.");
        }

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new AccountDisabledException("Your account is pending approval.");
        }
        
//        if (user.getRole() == Role.COMPANY_ADMIN && !user.isEmailVerified()) {
//            throw new AccountDisabledException("Please verify your email before logging in.");
//        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, password)
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new AccountDisabledException("Your account is disabled.");
        } catch (LockedException e) {
            throw new AccountLockedException("Your account is locked. Contact administrator.");
        } catch (InternalAuthenticationServiceException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(),
                user.getEmail()
        );

        refreshTokenService.createRefreshToken(user, refreshToken, deviceInfo, ipAddress);

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
    public AuthResponse refreshToken(RefreshTokenRequest request, String deviceInfo, String ipAddress) {
        String token = request.getRefreshToken();

        if (token == null || token.isBlank()) {
            throw new InvalidOperationException("Refresh token is required");
        }

        if (!jwtUtil.validateRefreshToken(token)) {
            throw new InvalidOperationException("Invalid or expired refresh token");
        }

        RefreshToken storedToken = refreshTokenService.verifyStoredToken(token);
        User user = storedToken.getUser();

        String newAccessToken = jwtUtil.generateAccessToken(user);

        String newRefreshToken = jwtUtil.generateRefreshToken(
                user.getId(),
                user.getEmail()
        );

        // rotate old refresh token
        refreshTokenService.revokeToken(storedToken);

        // save new refresh token
        refreshTokenService.createRefreshToken(user, newRefreshToken, deviceInfo, ipAddress);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
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