package com.lwd.jobportal.repository;

import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.enums.NoticeStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobSeekerRepository extends JpaRepository<JobSeeker, Long> {

	Optional<JobSeeker> findByUser_Id(Long userId);

    // Recruiter Filters
    List<JobSeeker> findByNoticeStatus(NoticeStatus status);

    List<JobSeeker> findByLastWorkingDayBetween(LocalDate start, LocalDate end);

    List<JobSeeker> findByImmediateJoinerTrue();

    List<JobSeeker> findByPreferredLocation(String location);

}
