package com.lwd.jobportal.repository;

import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.enums.NoticeStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobSeekerRepository extends
        JpaRepository<JobSeeker, Long>,
        JpaSpecificationExecutor<JobSeeker> {

    Optional<JobSeeker> findByUserId(Long userId);
    
    @Query("SELECT js FROM JobSeeker js WHERE js.user.id IN :userIds")
    List<JobSeeker> findByUserIds(@Param("userIds") List<Long> userIds);

    // Recruiter filters
    List<JobSeeker> findByNoticeStatus(NoticeStatus status);

    List<JobSeeker> findByLastWorkingDayBetween(LocalDate start, LocalDate end);

    List<JobSeeker> findByImmediateJoinerTrue();

    List<JobSeeker> findByPreferredLocation(String location);


    /*
     FETCH user and skills in one query
     Prevents N+1 problem
     */
    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<JobSeeker> findAll(Specification<JobSeeker> spec, Pageable pageable);


    @Modifying
    @Query("""
            UPDATE JobSeeker js
            SET js.profileCompletion = :percentage
            WHERE js.user.id = :userId
           """)
    void updateProfileCompletion(Long userId, Integer percentage);
}
