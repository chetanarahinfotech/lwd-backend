package com.lwd.jobportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (Login)
    Optional<User> findByEmail(String email);

    // Check if email already exists (Register)
    boolean existsByEmail(String email);

    // Find users by role (Admin / Recruiter / Job Seeker)
    List<User> findByRole(Role role);

    // Find active users
    List<User> findByIsActiveTrue();

	List<User> findByRoleAndCompany(Role role, Company company);
	
	List<User> findByRoleAndCompanyIdAndStatus(
	        Role role,
	        Long companyId,
	        UserStatus status
	);
	
	 long countByCompanyIdAndRole(Long companyId, Role role);

}
