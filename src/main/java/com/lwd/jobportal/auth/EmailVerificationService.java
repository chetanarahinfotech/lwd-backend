package com.lwd.jobportal.auth;

import com.lwd.jobportal.auth.dto.EmailVerificationRequestDTO;
import com.lwd.jobportal.auth.dto.ResendVerificationRequestDTO;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.util.EmailVerificationTokenUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationTokenUtil tokenUtil;
    private final EmailRateLimiterService rateLimiter;
    
    
	public void createAndSendToken(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("User email is required");
        }

        if (Boolean.TRUE.equals(user.isEmailVerified())) {
            return;
        }

        String email = user.getEmail().trim().toLowerCase();

        String token = tokenUtil.generateToken(email);

        emailService.sendVerificationEmail(email, token);
    }

    public boolean resendVerification(ResendVerificationRequestDTO request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return false;
        }

        String email = request.getEmail().trim().toLowerCase();

        if (!rateLimiter.isAllowed(email)) {
            log.warn("Resend blocked by rate limiter for {}", email);
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("Resend failed: user not found for {}", email);
            return false;
        }

        User user = userOpt.get();

        if (Boolean.TRUE.equals(user.isEmailVerified())) {
            log.info("Resend skipped: already verified {}", email);
            return false;
        }

        String token = tokenUtil.generateToken(email);
        emailService.sendVerificationEmail(email, token);

        log.info("Verification email resent to {}", email);
        return true;
    }

    @Transactional
    public boolean verifyToken(EmailVerificationRequestDTO request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            log.warn("Verification failed: token missing");
            return false;
        }

        try {
            String token = request.getToken().trim();
            String email = tokenUtil.extractEmail(token);

            if (email == null || email.isBlank()) {
                log.warn("Verification failed: extracted email is empty");
                return false;
            }

            email = email.trim().toLowerCase();

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                log.warn("Verification failed: user not found for {}", email);
                return false;
            }

            User user = userOpt.get();

            if (!tokenUtil.isTokenValid(token, email)) {
                log.warn("Verification failed: invalid or expired token for {}", email);
                return false;
            }

            if (Boolean.TRUE.equals(user.isEmailVerified())) {
                log.info("User already verified: {}", email);
                return true;
            }

            user.setEmailVerified(true);
            userRepository.save(user);

            log.info("Email verified successfully for {}", email);
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("Verification failed: token expired", e);
            return false;
        } catch (JwtException e) {
            log.warn("Verification failed: invalid token", e);
            return false;
        } catch (Exception e) {
            log.error("Verification failed due to unexpected error", e);
            return false;
        }
    }
}