package com.lwd.jobportal.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify your LWD account";
        String verificationUrl = frontendBaseUrl + "/verify-email?token=" + token;

        String body = """
                Hi,

                Thank you for registering on LWD.
                Please verify your email by clicking the link below:

                %s

                This link will expire in 24 hours.

                If you did not create an account, please ignore this email.

                Thanks,
                LWD Team
                """.formatted(verificationUrl);

        sendEmail(toEmail, subject, body);
        log.info("Verification email sent to {}", toEmail);
    }

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send email to {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Unable to send email at the moment. Please try again later.", e);
        }
    }
}