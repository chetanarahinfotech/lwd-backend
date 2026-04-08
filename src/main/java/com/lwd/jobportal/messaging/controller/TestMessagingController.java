package com.lwd.jobportal.messaging.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.JwtUtil;

import java.util.Map;

/**
 * TEMPORARY TEST CONTROLLER - DELETE AFTER VERIFICATION
 */
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestMessagingController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/token/{userId}")
    public ResponseEntity<?> getTestToken(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long uid = user.getId();
        if (uid == null) throw new IllegalArgumentException("User ID is null");

        String token = jwtUtil.generateToken(
                uid,
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "token", token
        ));
    }
}
