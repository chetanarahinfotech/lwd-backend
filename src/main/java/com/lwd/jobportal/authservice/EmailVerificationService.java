package com.lwd.jobportal.authservice;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.util.TokenUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public void createAndSendToken(User user) {
        String token = TokenUtil.generateToken();
        user.setEmailVerificationToken(token);
        user.setTokenExpiry(TokenUtil.getExpiry(24)); // valid 24 hours
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public boolean verifyToken(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) return false;

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return true;
    }
}
