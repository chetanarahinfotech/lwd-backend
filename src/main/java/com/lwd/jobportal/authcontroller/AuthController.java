package com.lwd.jobportal.authcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.authdto.JwtResponse;
import com.lwd.jobportal.authdto.LoginRequest;
import com.lwd.jobportal.authdto.RegisterRequest;
import com.lwd.jobportal.authdto.RegisterResponse;
import com.lwd.jobportal.authservice.AuthService;
import com.lwd.jobportal.entity.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request) {

        User user = authService.registerUser(request);

        // Build response DTO
        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),  
                user.getIsActive(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }


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
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

}
