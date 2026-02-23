package com.lwd.jobportal.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	Page<User> findByRoleAndCompany(Role role, Company company, Pageable pageable);
	
	Page<User> findByRoleAndCompanyIdAndStatus(
	        Role role,
	        Long companyId,
	        UserStatus status,
	        Pageable pageable
	);
	
	long countByCompanyIdAndRole(Long companyId, Role role);
	 
	long countByRole(Role role);
	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
	List<User> findTop5ByOrderByCreatedAtDesc();
	long countByCompanyIdAndRoleIn(Long companyId, Collection<Role> roles);
	List<User> findByCompanyIdAndRoleIn(Long companyId, Collection<Role> roles);

}
