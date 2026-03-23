 package com.lwd.jobportal.authcontroller;

import com.lwd.jobportal.authservice.PasswordResetService;
import com.lwd.jobportal.dto.authdto.ChangePasswordRequestDTO;
import com.lwd.jobportal.dto.authdto.ForgotPasswordRequestDTO;
import com.lwd.jobportal.dto.authdto.ResetPasswordRequestDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    private final PasswordResetService service;

    public PasswordController(PasswordResetService service) {
        this.service = service;
    }

    // Forgot password
    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDTO email) {

        boolean sent = service.sendResetLink(email);

        if (!sent) {
            return ResponseEntity.badRequest()
                    .body("User with this email does not exist");
        }

        return ResponseEntity.ok("Password reset email sent");
    }

    // Reset password
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(
            @RequestBody ResetPasswordRequestDTO request) {

        boolean reset = service.resetPassword(request);

        if (!reset)
            return ResponseEntity.badRequest().body("Invalid or expired token");

        return ResponseEntity.ok("Password reset successful");
    }

    
    @PostMapping("/change")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequestDTO request) {

        boolean changed = service.changePassword(request);

        if (!changed)
            return ResponseEntity.badRequest().body("Invalid credentials");

        return ResponseEntity.ok("Password changed successfully");
    }
    
    
    


}
