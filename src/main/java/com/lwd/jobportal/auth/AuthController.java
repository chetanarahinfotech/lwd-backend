package com.lwd.jobportal.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.auth.dto.AuthResponse;
import com.lwd.jobportal.auth.dto.LoginRequest;
import com.lwd.jobportal.auth.dto.RefreshTokenRequest;
import com.lwd.jobportal.auth.dto.RegisterRequest;
import com.lwd.jobportal.auth.dto.RegisterResponse;
import com.lwd.jobportal.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs for registration, login, refresh token, and logout")
public class AuthController {

    private final AuthService authService;

    // ================= REGISTER JOB SEEKER =================
    @Operation(
            summary = "Register job seeker",
            description = "Create a new job seeker account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job seeker registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/register/jobseeker")
    public ResponseEntity<RegisterResponse> registerJobSeeker(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job seeker registration request",
                    required = true
            )
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.registerJobSeeker(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(buildResponse(user));
    }

    // ================= REGISTER RECRUITER =================
    @Operation(
            summary = "Register recruiter",
            description = "Create a new recruiter account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recruiter registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/register/recruiter")
    public ResponseEntity<RegisterResponse> registerRecruiter(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Recruiter registration request",
                    required = true
            )
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.registerRecruiter(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(buildResponse(user));
    }
    
    
    @Operation(
            summary = "Register Company Admin",
            description = "Create a new Company Admin account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Company Admin registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/register/company-admin")
    public ResponseEntity<RegisterResponse> registerCompanyAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Company admin registration request",
                    required = true
            )
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.registerCompanyAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(buildResponse(user));
    }

    // ================= LOGIN =================
    @Operation(
            summary = "Login user",
            description = "Authenticate user and return access token + refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "403", description = "Account locked or disabled")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login request with email and password",
                    required = true
            )
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

    	AuthResponse response = authService.login(
                request.getEmail(),
                request.getPassword(),
                request.getDeviceId(), // ✅ FIXED
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr()
        );

        return ResponseEntity.ok(response);
    }

    // ================= REFRESH TOKEN =================
    @Operation(
            summary = "Refresh access token",
            description = "Generate a new access token and refresh token using a valid refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token request"),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token request",
                    required = true
            )
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.refreshToken(
                request,
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr()
        );

        return ResponseEntity.ok(response);
    }

    // ================= LOGOUT =================
    @Operation(
            summary = "Logout user",
            description = "Logout user by revoking refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token request"),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Logout request with refresh token",
                    required = true
            )
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok("Logged out successfully");
    }

    // ================= PRIVATE HELPER =================
    private RegisterResponse buildResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getStatus(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }
}