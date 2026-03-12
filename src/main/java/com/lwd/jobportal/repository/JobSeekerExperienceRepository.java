package com.lwd.jobportal.repository;

import com.lwd.jobportal.entity.JobSeekerExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSeekerExperienceRepository 
        extends JpaRepository<JobSeekerExperience, Long> {

    boolean existsByUserId(Long userId);

    List<JobSeekerExperience> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
