package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.service.SuperAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    // ================= VIEW ADMINS =================
    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAllAdmins() {
        return ResponseEntity.ok(superAdminService.getAllAdmins());
    }

    // ================= CREATE ADMIN =================
    @PostMapping("/admins")
    public ResponseEntity<String> createAdmin(@RequestBody User user) {
        superAdminService.createAdmin(user);
        return ResponseEntity.ok("Admin created successfully");
    }

    // ================= PROMOTE USER TO ADMIN =================
    @PutMapping("/admins/{userId}/promote")
    public ResponseEntity<String> promoteToAdmin(@PathVariable Long userId) {
        superAdminService.promoteToAdmin(userId);
        return ResponseEntity.ok("User promoted to ADMIN");
    }

    // ================= DEMOTE ADMIN =================
    @PutMapping("/admins/{adminId}/demote")
    public ResponseEntity<String> demoteAdmin(@PathVariable Long adminId) {
        superAdminService.demoteAdmin(adminId);
        return ResponseEntity.ok("Admin demoted to JOB_SEEKER");
    }

    // ================= BLOCK ADMIN =================
    @PutMapping("/admins/{adminId}/block")
    public ResponseEntity<String> blockAdmin(@PathVariable Long adminId) {
        superAdminService.blockAdmin(adminId);
        return ResponseEntity.ok("Admin blocked successfully");
    }

    // ================= UNBLOCK ADMIN =================
    @PutMapping("/admins/{adminId}/unblock")
    public ResponseEntity<String> unblockAdmin(@PathVariable Long adminId) {
        superAdminService.unblockAdmin(adminId);
        return ResponseEntity.ok("Admin unblocked successfully");
    }

    // ================= DELETE ADMIN =================
    @DeleteMapping("/admins/{adminId}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long adminId) {
        superAdminService.deleteAdmin(adminId);
        return ResponseEntity.ok("Admin deleted successfully");
    }

    // ================= CHANGE USER ROLE =================
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> changeUserRole(
            @PathVariable Long userId,
            @RequestParam Role newRole) {

        superAdminService.changeUserRole(userId, newRole);
        return ResponseEntity.ok("User role updated successfully");
    }
}
