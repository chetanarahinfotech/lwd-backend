package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.service.SuperAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin", description = "Super admin management APIs (Admin control & role management)")
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @Operation(
            summary = "Get all admins",
            description = "Fetch list of all admin users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admins fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only SUPER_ADMIN can access")
    })
    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAllAdmins() {
        return ResponseEntity.ok(superAdminService.getAllAdmins());
    }

    @Operation(
            summary = "Create admin",
            description = "Create a new admin user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/admins")
    public ResponseEntity<String> createAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin user details",
                    required = true
            )
            @RequestBody User user) {

        superAdminService.createAdmin(user);
        return ResponseEntity.ok("Admin created successfully");
    }

    @Operation(
            summary = "Promote user to admin",
            description = "Promote a normal user to ADMIN role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User promoted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/admins/{userId}/promote")
    public ResponseEntity<String> promoteToAdmin(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {

        superAdminService.promoteToAdmin(userId);
        return ResponseEntity.ok("User promoted to ADMIN");
    }

    @Operation(
            summary = "Demote admin",
            description = "Demote admin to JOB_SEEKER role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin demoted successfully"),
            @ApiResponse(responseCode = "404", description = "Admin not found")
    })
    @PutMapping("/admins/{adminId}/demote")
    public ResponseEntity<String> demoteAdmin(
            @Parameter(description = "Admin ID")
            @PathVariable Long adminId) {

        superAdminService.demoteAdmin(adminId);
        return ResponseEntity.ok("Admin demoted to JOB_SEEKER");
    }

    @Operation(
            summary = "Block admin",
            description = "Block an admin user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin blocked successfully"),
            @ApiResponse(responseCode = "404", description = "Admin not found")
    })
    @PutMapping("/admins/{adminId}/block")
    public ResponseEntity<String> blockAdmin(
            @Parameter(description = "Admin ID")
            @PathVariable Long adminId) {

        superAdminService.blockAdmin(adminId);
        return ResponseEntity.ok("Admin blocked successfully");
    }

    @Operation(
            summary = "Unblock admin",
            description = "Unblock an admin user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin unblocked successfully"),
            @ApiResponse(responseCode = "404", description = "Admin not found")
    })
    @PutMapping("/admins/{adminId}/unblock")
    public ResponseEntity<String> unblockAdmin(
            @Parameter(description = "Admin ID")
            @PathVariable Long adminId) {

        superAdminService.unblockAdmin(adminId);
        return ResponseEntity.ok("Admin unblocked successfully");
    }

    @Operation(
            summary = "Delete admin",
            description = "Delete an admin user permanently"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Admin not found")
    })
    @DeleteMapping("/admins/{adminId}")
    public ResponseEntity<String> deleteAdmin(
            @Parameter(description = "Admin ID")
            @PathVariable Long adminId) {

        superAdminService.deleteAdmin(adminId);
        return ResponseEntity.ok("Admin deleted successfully");
    }

    @Operation(
            summary = "Change user role",
            description = "Change role of any user (e.g., JOB_SEEKER → ADMIN)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Parameters({
            @Parameter(name = "userId", description = "User ID"),
            @Parameter(name = "newRole", description = "New role (ADMIN, RECRUITER, JOB_SEEKER, etc.)")
    })
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> changeUserRole(
            @PathVariable Long userId,
            @RequestParam Role newRole) {

        superAdminService.changeUserRole(userId, newRole);
        return ResponseEntity.ok("User role updated successfully");
    }
}