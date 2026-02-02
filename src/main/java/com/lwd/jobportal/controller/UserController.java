package com.lwd.jobportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.UserService;
import com.lwd.jobportal.userdto.UpdateUserRequest;
import com.lwd.jobportal.userdto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

  
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {

    	Long userId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                userService.getUserById(userId)
        );
    }

   
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateMyProfile(
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        Long userId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                userService.updateUser(userId, request)
        );
    }
}
