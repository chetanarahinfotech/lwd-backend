package com.lwd.jobportal.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.enums.JobStatus;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
	
	@EntityGraph(attributePaths = {"company"})
	Page<Job> findByCompanyId(Long companyId, Pageable pageable);
	
    List<Job> findByCreatedById(Long userId);
    
    List<Job> findByStatusAndCreatedAtLessThanOrderByCreatedAtDesc(
            JobStatus status,
            LocalDateTime lastSeen,
            Pageable pageable
    );
    
    @Query("""
    		SELECT j FROM Job j
    		JOIN FETCH j.company
    		WHERE j.status = :status
    		ORDER BY j.createdAt DESC
    		""")
    List<Job> findLatestJobsWithCompany(JobStatus status, Pageable pageable);

    Page<Job> findByIndustryIgnoreCaseAndStatus(
            String industry,
            JobStatus status,
            Pageable pageable
    );
    

}
