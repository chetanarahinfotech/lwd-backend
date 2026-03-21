package com.lwd.jobportal.authservice;

import com.lwd.jobportal.dto.authdto.EmailVerificationRequestDTO;
import com.lwd.jobportal.dto.authdto.ResendVerificationRequestDTO;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.util.EmailVerificationTokenUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailRateLimiterService rateLimiter;

    /**
     * Send verification email during registration
     */
    public void createAndSendToken(User user) {

        String token = EmailVerificationTokenUtil.generateToken(user.getEmail());

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    /**
     * Resend verification email
     */
    public boolean resendVerification(ResendVerificationRequestDTO request) {

        String email = request.getEmail();

        // Rate limit protection
        if (!rateLimiter.isAllowed(email)) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        // Already verified
        if (user.isEmailVerified()) return false;

        String token = EmailVerificationTokenUtil.generateToken(email);

        emailService.sendVerificationEmail(email, token);

        return true;
    }

    /**
     * Verify email using JWT token
     */
    public boolean verifyToken(EmailVerificationRequestDTO request) {

        try {

            String email =
                    EmailVerificationTokenUtil.extractEmail(request.getToken());

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) return false;

            User user = userOpt.get();

            if (user.isEmailVerified()) return true;

            user.setEmailVerified(true);

            userRepository.save(user);

            return true;

        } catch (Exception ex) {
            return false;
        }
    }
}
