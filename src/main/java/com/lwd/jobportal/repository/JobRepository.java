package com.lwd.jobportal.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
	
	@EntityGraph(attributePaths = {"company"})
	Page<Job> findByCompanyId(Long companyId, Pageable pageable);
	
    List<Job> findByCreatedById(Long userId);
    Page<Job> findByCreatedById(Long userId, Pageable pageable);
    
    long countByCompanyId(Long companyId);
    
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
    
    @Query("""
    	    SELECT j FROM Job j
    	    JOIN j.company c
    	    WHERE j.status = 'OPEN'
    	    AND (
    	        LOWER(j.title) LIKE %:keyword%
    	        OR LOWER(j.industry) LIKE %:keyword%
    	        OR LOWER(c.companyName) LIKE %:keyword%
    	    )
    	""")
    	Page<Job> quickSearch(@Param("keyword") String keyword, Pageable pageable);

    
    @Query("""
    	    SELECT j FROM Job j
    	    WHERE j.status = 'OPEN'
    	    AND j.location = :location
    	    AND j.industry = :industry
    	    ORDER BY j.createdAt DESC
    	""")
    	List<Job> findSuggestedJobs(
    	        @Param("location") String location,
    	        @Param("industry") String industry
    	);

    
    @Query("""
    	    SELECT a.job FROM JobApplication a
    	    WHERE a.jobSeeker.id = :userId
    	    ORDER BY a.appliedAt DESC
    	""")
    	Optional<Job> findLastAppliedJob(@Param("userId") Long userId);

    
    @Query("""
    	    SELECT j FROM Job j
    	    WHERE j.status = 'OPEN'
    	    AND j.industry = :industry
    	    AND j.jobType = :jobType
    	    AND j.id <> :jobId
    	""")
    	List<Job> findSimilarJobs(
    	        @Param("industry") String industry,
    	        @Param("jobType") JobType jobType,
    	        @Param("jobId") Long jobId
    	);

    
    @Query("""
    	    SELECT DISTINCT j.title FROM Job j
    	    WHERE LOWER(j.title) LIKE %:keyword%
    	""")
    	List<String> findTitleSuggestions(@Param("keyword") String keyword);

    
    @Query("""
    	    SELECT j FROM Job j
    	    WHERE j.status = 'OPEN'
    	    ORDER BY j.viewCount DESC
    	""")
    	List<Job> findTrendingJobs(Pageable pageable);

    

}
