package com.lwd.jobportal.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.enums.ApplicationStatus;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {

    // 🔹 Prevent duplicate apply (PORTAL)
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    // 🔹 Recruiter / Company Admin: applications for a job (paginated)
//    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
    
    Page<JobApplication> findByJobIdAndJobCompanyId(
            Long jobId,
            Long companyId,
            Pageable pageable
    );
    
    
    @EntityGraph(attributePaths = {
            "job",
            "job.company",
            "jobSeeker",
            "jobSeeker.jobSeekerProfile"
    })
    Page<JobApplication> findByJob_Id(Long jobId, Pageable pageable);
    
    
    @Query("""
    	    SELECT ja.status, COUNT(ja)
    	    FROM JobApplication ja
    	    WHERE ja.job.id = :jobId
    	    GROUP BY ja.status
    	""")
    	List<Object[]> countApplicationsByStatus(Long jobId);
    
    // ADMIN → all applications
    @EntityGraph(attributePaths = {
    	        "job",
    	        "job.company",
    	        "jobSeeker",
    	        "jobSeeker.jobSeekerProfile"
    	})
    	Page<JobApplication> findAll(Pageable pageable);

    // RECRUITER_ADMIN → company jobs
    @EntityGraph(attributePaths = {
            "job",
            "job.company",
            "jobSeeker",
            "jobSeeker.jobSeekerProfile"
    })
    Page<JobApplication> findByJobCompanyId(Long companyId, Pageable pageable);

    // RECRUITER → only jobs created by this recruiter
    @EntityGraph(attributePaths = {
            "job",
            "job.company",
            "jobSeeker",
            "jobSeeker.jobSeekerProfile"
    })
    Page<JobApplication> findByJobCreatedById(Long userId, Pageable pageable);

    // 🔹 Job Seeker: my applications (paginated)
    @EntityGraph(attributePaths = {
            "job",
            "job.company",
            "jobSeeker",
            "jobSeeker.jobSeekerProfile"
    })
    Page<JobApplication> findByJobSeekerId(Long jobSeekerId, Pageable pageable);
    
    @Query("""
    	    SELECT ja FROM JobApplication ja
    	    JOIN FETCH ja.jobSeeker
    	    WHERE ja.job.id = :jobId
    	""")
    	Page<JobApplication> findByJobIdWithJobSeeker(
    	        @Param("jobId") Long jobId,
    	        Pageable pageable
    	);


    // 🔹 Admin: filter by status (paginated)
    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    // 🔹 Recruiter: job + status filter (paginated)
    @EntityGraph(attributePaths = {
            "job",
            "job.company",
            "jobSeeker"
    })
    Page<JobApplication> findByJobIdAndStatus(
            Long jobId,
            ApplicationStatus status,
            Pageable pageable
    );

    // 🔹 Job Seeker: view specific application safely
    Page<JobApplication> findByIdAndJobSeekerId(
            Long id,
            Long jobSeekerId,
            Pageable pageable
    );

    Page<JobApplication> findByJobCompanyIdAndStatus(
            Long companyId,
            ApplicationStatus status,
            Pageable pageable
    );
    
    
    
    @Query("""
    	    SELECT ja.job.id, COUNT(ja)
    	    FROM JobApplication ja
    	    WHERE ja.job.id IN :jobIds
    	    GROUP BY ja.job.id
    	""")
    	List<Object[]> countApplicationsForJobs(List<Long> jobIds);
    
    long countByAppliedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByJobCompanyId(Long companyId);
    long countByJobCreatedById(Long recruiterId);
    
    @Query("SELECT DATE(ja.appliedAt) as day, COUNT(ja) as cnt FROM JobApplication ja " +
           "WHERE ja.appliedAt >= :weekAgo GROUP BY DATE(ja.appliedAt)")
    List<Object[]> countApplicationsPerDay(LocalDateTime weekAgo);
    
    List<JobApplication> findTop5ByOrderByAppliedAtDesc();
    
    // funnel queries for company
    @Query("SELECT ja.status, COUNT(ja) FROM JobApplication ja WHERE ja.job.company.id = :companyId GROUP BY ja.status")
    List<Object[]> countByStatusForCompany(Long companyId);
    
    long countByJobCreatedByIdAndStatus(Long recruiterId, ApplicationStatus status);
    long countByJobId(Long jobId);
    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);
    long countByJobIdAndStatusIn(Long jobId, Collection<ApplicationStatus> statuses);
    List<JobApplication> findTop5ByJobCreatedByIdOrderByAppliedAtDesc(Long recruiterId);
  

}
