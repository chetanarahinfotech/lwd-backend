package com.lwd.jobportal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.enums.ApplicationStatus;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // 🔹 Prevent duplicate apply (PORTAL)
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    // 🔹 Recruiter / Company Admin: applications for a job (paginated)
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
    
    Page<JobApplication> findByJobIdAndJobCompanyId(
            Long jobId,
            Long companyId,
            Pageable pageable
    );

    // 🔹 Job Seeker: my applications (paginated)
    Page<JobApplication> findByJobSeekerId(Long jobSeekerId, Pageable pageable);

    // 🔹 Admin: filter by status (paginated)
    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    // 🔹 Recruiter: job + status filter (paginated)
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
    
    Page<JobApplication> findByJobCompanyId(
            Long companyId,
            Pageable pageable
    );

    Page<JobApplication> findByJobCompanyIdAndStatus(
            Long companyId,
            ApplicationStatus status,
            Pageable pageable
    );

}
