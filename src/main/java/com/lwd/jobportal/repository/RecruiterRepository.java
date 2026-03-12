package com.lwd.jobportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lwd.jobportal.entity.Recruiter;

public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {

    Optional<Recruiter> findByUserId(Long userId);

}
