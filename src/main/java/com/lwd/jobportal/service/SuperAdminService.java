package com.lwd.jobportal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ForbiddenActionException;
import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final UserRepository userRepository;

    // ================= VIEW ADMINS =================
    public List<User> getAllAdmins() {
        requireSuperAdmin();
        return userRepository.findByRole(Role.ADMIN);
    }

    // ================= CREATE ADMIN =================
    public void createAdmin(User user) {
        requireSuperAdmin();

        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    // ================= PROMOTE TO ADMIN =================
    public void promoteToAdmin(Long userId) {
        requireSuperAdmin();

        User user = getUser(userId);

        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenActionException("Cannot modify SUPER_ADMIN");
        }

        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    // ================= DEMOTE ADMIN =================
    public void demoteAdmin(Long adminId) {
        requireSuperAdmin();

        User admin = getUser(adminId);

        if (admin.getRole() != Role.ADMIN) {
            throw new InvalidOperationException("User is not ADMIN");
        }

        admin.setRole(Role.JOB_SEEKER);
        userRepository.save(admin);
    }

    public void blockAdmin(Long adminId) {

        requireSuperAdmin();

        Long currentUserId = SecurityUtils.getUserId();
        User admin = getUser(adminId);

        if (admin.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenActionException("Cannot lock SUPER_ADMIN");
        }

        if (admin.getId().equals(currentUserId)) {
            throw new ForbiddenActionException("You cannot lock yourself");
        }

        admin.setLocked(true);
        userRepository.save(admin);
    }


    // ================= UNBLOCK ADMIN =================
    public void unblockAdmin(Long adminId) {

        requireSuperAdmin();

        User admin = getUser(adminId);

        if (!admin.isLocked()) {
            throw new RuntimeException("Admin is not locked");
        }

        admin.setLocked(false);
        userRepository.save(admin);
    }


    // ================= DELETE ADMIN =================
    public void deleteAdmin(Long adminId) {
        requireSuperAdmin();

        Long currentUserId = SecurityUtils.getUserId();

        if (currentUserId.equals(adminId)) {
            throw new RuntimeException("You cannot delete yourself");
        }

        User admin = getUser(adminId);

        if (admin.getRole() != Role.ADMIN) {
            throw new InvalidOperationException("User is not ADMIN");
        }

        userRepository.delete(admin);
    }

    // ================= CHANGE ROLE =================
    public void changeUserRole(Long userId, Role newRole) {
        requireSuperAdmin();

        User user = getUser(userId);

        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenActionException("Cannot modify SUPER_ADMIN");
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    // ================= HELPER METHODS =================
    private void requireSuperAdmin() {
        Role role = SecurityUtils.getRole();

        if (role != Role.SUPER_ADMIN) {
            throw new ForbiddenActionException(
                    "Only SUPER_ADMIN can perform this action"
            );
        }
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
