package com.lwd.jobportal.authcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.authservice.AuthService;
import com.lwd.jobportal.dto.authdto.JwtResponse;
import com.lwd.jobportal.dto.authdto.LoginRequest;
import com.lwd.jobportal.dto.authdto.RegisterRequest;
import com.lwd.jobportal.dto.authdto.RegisterResponse;
import com.lwd.jobportal.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ================= REGISTER JOB SEEKER =================
    @PostMapping("/register/jobseeker")
    public ResponseEntity<RegisterResponse> registerJobSeeker(
            @RequestBody RegisterRequest request) {

        User user = authService.registerJobSeeker(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(buildResponse(user));
    }

    // ================= REGISTER RECRUITER =================
    @PostMapping("/register/recruiter")
    public ResponseEntity<RegisterResponse> registerRecruiter(
            @RequestBody RegisterRequest request) {

        User user = authService.registerRecruiter(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(buildResponse(user));
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {

            String token = authService.login(
                    request.getEmail(),
                    request.getPassword()
            );

            return ResponseEntity.ok(new JwtResponse(token));

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
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
