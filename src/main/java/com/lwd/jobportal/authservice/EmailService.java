package com.lwd.jobportal.authservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * EmailService handles sending all types of emails in LWD.
 * Currently used for email verification for all user roles.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a verification email to the user.
     *
     * @param toEmail The recipient's email address.
     * @param token   The unique verification token.
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify your LWD account";
        // Link points to your frontend verification page
        String verificationUrl = "http://localhost:3000/verify-email?token=" + token;

        String body = """
                Hi,
                
                Thank you for registering on LWD.
                Please verify your email by clicking the link below:
                
                %s
                
                If you did not create an account, please ignore this email.
                
                Thanks,
                LWD Team
                """.formatted(verificationUrl);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
    
    
    

    /**
     * Optional: Generic method to send any email
     *
     * @param toEmail Recipient email
     * @param subject Email subject
     * @param body    Email content
     */
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
