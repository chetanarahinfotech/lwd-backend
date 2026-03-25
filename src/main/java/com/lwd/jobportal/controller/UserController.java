package com.lwd.jobportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.userdto.UpdateUserRequest;
import com.lwd.jobportal.dto.userdto.UserResponse;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.UserActivityService;
import com.lwd.jobportal.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and active status APIs")
public class UserController {

    private final UserService userService;
    private final UserActivityService userActivityService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my profile",
            description = "Fetch profile details of the logged-in user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {

        Long userId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                userService.getUserById(userId)
        );
    }

    @Operation(
            summary = "Get user profile by ID",
            description = "Fetch profile details of a user by user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfileById(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update my profile",
            description = "Update profile details of the logged-in user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateMyProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user profile request",
                    required = true
            )
            @RequestBody UpdateUserRequest request) {

        Long userId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                userService.updateUser(userId, request)
        );
    }

    @Operation(
            summary = "Check user active status by ID",
            description = "Check whether a user is currently active by user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active status fetched successfully")
    })
    
    @GetMapping("/{userId}/active")
    public boolean isUserActive(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        return userActivityService.isUserActive(userId);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Check my active status",
            description = "Check whether the logged-in user is currently active"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active status fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    
    @GetMapping("/active")
    public boolean isMeActive() {
        Long userId = SecurityUtils.getUserId();
        return userActivityService.isUserActive(userId);
    }
}