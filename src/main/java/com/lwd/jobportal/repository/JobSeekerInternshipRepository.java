package com.lwd.jobportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.JobSeekerInternship;

@Repository
public interface JobSeekerInternshipRepository extends JpaRepository<JobSeekerInternship, Long> {

    List<JobSeekerInternship> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);

}
