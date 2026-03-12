package com.lwd.jobportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.JobSeekerCertification;

@Repository
public interface JobSeekerCertificationRepository extends JpaRepository<JobSeekerCertification, Long> {

    List<JobSeekerCertification> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);

}
