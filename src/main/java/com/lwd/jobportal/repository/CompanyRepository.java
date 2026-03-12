package com.lwd.jobportal.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lwd.jobportal.dto.companydto.CompanyAnalyticsDTO;
import com.lwd.jobportal.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByCompanyName(String companyName);

    Optional<Company> findByCompanyName(String companyName);

    List<Company> findByIsActiveTrue();

    Optional<Company> findByCreatedById(Long userId);
    
    Page<Company> findByIndustry(String industry, Pageable pageable);
    
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT ja.status, COUNT(ja) FROM JobApplication ja WHERE ja.job.company.id = :companyId GROUP BY ja.status")
    List<Object[]> countByStatusForCompany(Long companyId);
    
    
    @Query("""
    	    SELECT new com.lwd.jobportal.dto.companydto.CompanyAnalyticsDTO(
    	        COUNT(DISTINCT j.id),
    	        COUNT(DISTINCT r.id),
    	        COUNT(DISTINCT a.id),
    	        COUNT(DISTINCT CASE WHEN j.status = com.lwd.jobportal.enums.JobStatus.OPEN THEN j.id END),
    	        COUNT(DISTINCT CASE WHEN j.status = com.lwd.jobportal.enums.JobStatus.CLOSED THEN j.id END)
    	    )
    	    FROM Company c
    	    LEFT JOIN Job j ON j.company.id = c.id
    	    LEFT JOIN User r ON r.company.id = c.id 
    	        AND r.role = com.lwd.jobportal.enums.Role.RECRUITER
    	    LEFT JOIN JobApplication a ON a.job.id = j.id
    	    WHERE c.id = :companyId
    	""")
    	CompanyAnalyticsDTO getCompanyAnalytics(@Param("companyId") Long companyId);
    
    
    @Query("""
    		SELECT c FROM Company c
    		WHERE c.isActive = true
    		AND LOWER(c.companyName) LIKE LOWER(CONCAT(:keyword, '%'))
    		ORDER BY c.companyName ASC
    		""")
    		Page<Company> searchCompanySuggestions(
    		        @Param("keyword") String keyword,
    		        Pageable pageable
    		);


    @Query("""
            SELECT c FROM Company c
            WHERE c.isActive = true
            AND (
                :keyword IS NULL OR
                LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(c.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(c.industry) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
        """)
        Page<Company> searchCompanies(
                @Param("keyword") String keyword,
                Pageable pageable
        );

}
