package com.lwd.jobportal.authcontroller;

import com.lwd.jobportal.authservice.PasswordResetService;
import com.lwd.jobportal.dto.authdto.ChangePasswordRequestDTO;
import com.lwd.jobportal.dto.authdto.ForgotPasswordRequestDTO;
import com.lwd.jobportal.dto.authdto.ResetPasswordRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@Tag(name = "Password", description = "Password management APIs including forgot, reset, and change password")
public class PasswordController {

    private final PasswordResetService service;

    public PasswordController(PasswordResetService service) {
        this.service = service;
    }

    @Operation(
            summary = "Forgot password",
            description = "Send password reset link to the user's email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @ApiResponse(responseCode = "400", description = "User with this email does not exist")
    })
    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Forgot password request containing email",
                    required = true
            )
            @org.springframework.web.bind.annotation.RequestBody ForgotPasswordRequestDTO email
    ) {

        boolean sent = service.sendResetLink(email);

        if (!sent) {
            return ResponseEntity.badRequest()
                    .body("User with this email does not exist");
        }

        return ResponseEntity.ok("Password reset email sent");
    }

    @Operation(
            summary = "Reset password",
            description = "Reset password using reset token and new password"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reset password request containing token and new password",
                    required = true
            )
            @org.springframework.web.bind.annotation.RequestBody ResetPasswordRequestDTO request
    ) {

        boolean reset = service.resetPassword(request);

        if (!reset) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }

        return ResponseEntity.ok("Password reset successful");
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Change password",
            description = "Change password for logged-in user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/change")
    public ResponseEntity<String> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Change password request containing current and new password",
                    required = true
            )
            @org.springframework.web.bind.annotation.RequestBody ChangePasswordRequestDTO request
    ) {

        boolean changed = service.changePassword(request);

        if (!changed) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        return ResponseEntity.ok("Password changed successfully");
    }
}