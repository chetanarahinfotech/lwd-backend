package com.lwd.jobportal.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.recruiter.RecruiterPerformanceDTO;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // Find user by email (Login)
    Optional<User> findByEmail(String email);

    // Check if email already exists (Register)
    boolean existsByEmail(String email);

    // Find users by role (Admin / Recruiter / Job Seeker)
    List<User> findByRole(Role role);

    // Find active users
    List<User> findByIsActiveTrue();
    
    @Modifying
    @Query("""
        UPDATE User u
        SET u.lastActiveAt = CURRENT_TIMESTAMP,
            u.isActive = true
        WHERE u.id IN :userIds
    """)
    int updateUsersLastActive(@Param("userIds") List<Long> userIds);


    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id NOT IN :userIds")
    void deactivateInactiveUsers(List<Long> userIds);

    

	Page<User> findByRoleAndCompany(Role role, Company company, Pageable pageable);
	
	Page<User> findByRoleAndCompanyIdAndStatus(
	        Role role,
	        Long companyId,
	        UserStatus status,
	        Pageable pageable
	);
	
	 @Override
	    @EntityGraph(attributePaths = {
	        "company"
	    })
	    Page<User> findAll(org.springframework.data.jpa.domain.Specification<User> spec, Pageable pageable);

	    @Override
	    @EntityGraph(attributePaths = {
	        "company"
	    })
	    Page<User> findAll(Pageable pageable);

	
	long countByCompanyIdAndRole(Long companyId, Role role);
	 
	long countByRole(Role role);
	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
	
	List<User> findByCompanyIdAndRoleIn(Long companyId, Collection<Role> roles);
	long countByCompanyIdAndRoleIn(Long companyId, Collection<Role> roles);
	
	@EntityGraph(attributePaths = {"company"})
	Optional<User> findWithCompanyById(Long id);
	
	@Query("""
		    SELECT new com.lwd.jobportal.recruiter.RecruiterPerformanceDTO(
		        u.id,
		        u.name,
		        COUNT(DISTINCT j.id),
		        COUNT(DISTINCT ja.id),
		        COUNT(DISTINCT CASE WHEN j.status = com.lwd.jobportal.enums.JobStatus.OPEN THEN j.id END)
		    )
		    FROM User u
		    LEFT JOIN Job j ON j.createdBy.id = u.id
		    LEFT JOIN JobApplication ja ON ja.job.createdBy.id = u.id
		    WHERE u.company.id = :companyId
		      AND u.role IN :roles
		    GROUP BY u.id, u.name
		    ORDER BY u.name ASC
		""")
		List<RecruiterPerformanceDTO> findRecruiterPerformanceByCompanyId(
		        @Param("companyId") Long companyId,
		        @Param("roles") Collection<Role> roles
		);
	
	@Query("SELECT u FROM User u WHERE u.role = 'JOB_SEEKER'")
	Page<User> findJobSeekers(Pageable pageable);


	@EntityGraph(attributePaths = {"company"})
	List<User> findTop5ByOrderByCreatedAtDesc();

	@EntityGraph(attributePaths = {"company"})
	@Query("SELECT u FROM User u WHERE u.role IN ('RECRUITER','COMPANY_ADMIN')")
	Page<User> findRecruiters(Pageable pageable);
	
	@Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
	Page<User> findAllOrderByCreatedAtDesc(Pageable pageable);

	default List<User> findRecentUsers(int size) {
	    return findAllOrderByCreatedAtDesc(PageRequest.of(0, size)).getContent();
	}
	
	
	
	@Query("""
			SELECT u FROM User u
			WHERE u.role = com.lwd.jobportal.enums.Role.JOB_SEEKER
			AND (
			    LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
			    OR u.phone LIKE CONCAT('%', :keyword, '%')
			)
			""")
			Page<User> searchJobSeekers(String keyword, Pageable pageable);

	@Query("""
			SELECT u FROM User u
			LEFT JOIN u.company c
			WHERE (u.role = com.lwd.jobportal.enums.Role.RECRUITER 
			   OR u.role = com.lwd.jobportal.enums.Role.COMPANY_ADMIN)
			AND (
			    LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
			    OR u.phone LIKE CONCAT('%', :keyword, '%')
			    OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			""")
			Page<User> searchRecruiters(String keyword, Pageable pageable);

	@Query("""
		    SELECT u FROM User u
		    WHERE u.role = com.lwd.jobportal.enums.Role.JOB_SEEKER
		    AND (
		        LOWER(u.name) LIKE LOWER(CONCAT(:keyword, '%'))
		        OR LOWER(u.email) LIKE LOWER(CONCAT(:keyword, '%'))
		        OR u.phone LIKE CONCAT(:keyword, '%')
		    )
		    ORDER BY u.name ASC
		""")
		Page<User> searchJobSeekerSuggestions(
		        @Param("keyword") String keyword,
		        Pageable pageable
		);

	
	@Query("""
		    SELECT u FROM User u
		    LEFT JOIN u.company c
		    WHERE (
		        u.role = com.lwd.jobportal.enums.Role.RECRUITER
		        OR u.role = com.lwd.jobportal.enums.Role.COMPANY_ADMIN
		    )
		    AND (
		        LOWER(u.name) LIKE LOWER(CONCAT(:keyword, '%'))
		        OR LOWER(u.email) LIKE LOWER(CONCAT(:keyword, '%'))
		        OR u.phone LIKE CONCAT(:keyword, '%')
		        OR LOWER(c.companyName) LIKE LOWER(CONCAT(:keyword, '%'))
		    )
		    ORDER BY u.name ASC
		""")
		Page<User> searchRecruiterSuggestions(
		        @Param("keyword") String keyword,
		        Pageable pageable
		);

	@Query("""
		    SELECT u.company.id, COUNT(u.id)
		    FROM User u
		    WHERE u.company.id IN :companyIds
		      AND u.role = :role
		    GROUP BY u.company.id
		""")
		List<Object[]> countByCompanyIdsAndRole(@Param("companyIds") List<Long> companyIds,
		                                        @Param("role") Role role);





}
