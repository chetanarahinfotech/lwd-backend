package com.lwd.jobportal.authservice;

import com.lwd.jobportal.dto.authdto.ChangePasswordRequestDTO;
import com.lwd.jobportal.dto.authdto.ForgotPasswordRequestDTO;
import com.lwd.jobportal.dto.authdto.ResetPasswordRequestDTO;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.util.PasswordResetTokenUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Forgot password
    public boolean sendResetLink(ForgotPasswordRequestDTO request) {

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) return false;

        String token = PasswordResetTokenUtil.generateToken(request.getEmail());

        String resetUrl =
                "http://localhost:5173/reset-password?token=" + token;

        String body = """
                Click the link below to reset your password:

                %s

                This link expires in 15 minutes.
                """.formatted(resetUrl);

        emailService.sendEmail(request.getEmail(),
                "Reset your LWD password",
                body);

        return true;
    }

    // Reset password
    public boolean resetPassword(ResetPasswordRequestDTO request) {

        try {

            String email =
                    PasswordResetTokenUtil.extractEmail(request.getToken());

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) return false;

            User user = userOpt.get();

            user.setPassword(
                    passwordEncoder.encode(request.getNewPassword())
            );

            userRepository.save(user);

            return true;

        } catch (Exception ex) {

            return false;
        }
    }


    // Change password
    public boolean changePassword(ChangePasswordRequestDTO request) {

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            return false;

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return true;
    }
}
