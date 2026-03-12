package com.lwd.jobportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.JobSeekerProject;

@Repository
public interface JobSeekerProjectRepository extends JpaRepository<JobSeekerProject, Long> {

    List<JobSeekerProject> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);

}
