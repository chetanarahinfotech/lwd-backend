package com.lwd.jobportal.authcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.authservice.EmailVerificationService;

@RestController
@RequestMapping("/users")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService verificationService;

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean verified = verificationService.verifyToken(token);
        if (!verified) return ResponseEntity.badRequest().body("Invalid or expired token");

        return ResponseEntity.ok("Email verified successfully!");
    }
}
