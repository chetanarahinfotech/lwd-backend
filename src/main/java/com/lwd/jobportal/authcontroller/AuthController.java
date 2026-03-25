package com.lwd.jobportal.authcontroller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.authservice.AuthService;
import com.lwd.jobportal.dto.authdto.JwtResponse;
import com.lwd.jobportal.dto.authdto.LoginRequest;
import com.lwd.jobportal.dto.authdto.RegisterRequest;
import com.lwd.jobportal.dto.authdto.RegisterResponse;
import com.lwd.jobportal.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs for registration and login")
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

    // ================= LOGIN =================
    @Operation(
            summary = "Login user",
            description = "Authenticate user and return JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "403", description = "Account locked or disabled")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login request with email and password",
                    required = true
            )
            @Valid @RequestBody LoginRequest request) {

        String token = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(new JwtResponse(token));
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