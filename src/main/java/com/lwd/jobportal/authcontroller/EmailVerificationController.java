package com.lwd.jobportal.authcontroller;

import com.lwd.jobportal.authservice.EmailVerificationService;
import com.lwd.jobportal.dto.authdto.EmailVerificationRequestDTO;
import com.lwd.jobportal.dto.authdto.ResendVerificationRequestDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailVerificationController {

    private final EmailVerificationService service;

    public EmailVerificationController(EmailVerificationService service) {
        this.service = service;
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestBody EmailVerificationRequestDTO request) {

        boolean verified = service.verifyToken(request);

        if (!verified) {
            return ResponseEntity.badRequest()
                    .body("Invalid or expired verification link");
        }

        return ResponseEntity.ok("Email verified successfully");
    }

    
    @PostMapping("/resend")
    public ResponseEntity<String> resendVerification(
            @RequestBody ResendVerificationRequestDTO request) {

        boolean sent = service.resendVerification(request);

        if (!sent) {
            return ResponseEntity.badRequest()
                    .body("Email already verified or resend not allowed");
        }

        return ResponseEntity.ok("Verification email sent again");
    }

}
