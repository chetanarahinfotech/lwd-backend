package com.lwd.jobportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.UserServiceImpl;
import com.lwd.jobportal.userdto.UpdateUserRequest;
import com.lwd.jobportal.userdto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

  
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {

    	Long userId = (Long) authentication.getPrincipal();
    	Long id = SecurityUtils.getUserId();

    	System.out.println(" Current user Id " + id);
        return ResponseEntity.ok(
                userService.getUserById(userId)
        );
    }

   
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateMyProfile(
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal(); // from JWT

        return ResponseEntity.ok(
                userService.updateUser(userId, request)
        );
    }
}
