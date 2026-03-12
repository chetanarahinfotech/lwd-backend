package com.lwd.jobportal.repository;

import com.lwd.jobportal.entity.JobSeekerEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSeekerEducationRepository 
        extends JpaRepository<JobSeekerEducation, Long> {

    boolean existsByUserId(Long userId);

    List<JobSeekerEducation> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
