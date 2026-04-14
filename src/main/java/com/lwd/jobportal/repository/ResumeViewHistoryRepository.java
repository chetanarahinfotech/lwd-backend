package com.lwd.jobportal.repository;

import com.lwd.jobportal.dto.resume.ResumeViewCountProjection;
import com.lwd.jobportal.entity.ResumeViewHistory;
import com.lwd.jobportal.enums.Role;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ResumeViewHistoryRepository extends JpaRepository<ResumeViewHistory, Long> {

    Page<ResumeViewHistory> findByResumeIdOrderByViewedAtDesc(Long resumeId, Pageable pageable);

    Page<ResumeViewHistory> findByOwnerUserIdOrderByViewedAtDesc(Long ownerUserId, Pageable pageable);

    Page<ResumeViewHistory> findByViewerIdOrderByViewedAtDesc(Long viewerId, Pageable pageable);

    Page<ResumeViewHistory> findByResumeIdAndViewSourceOrderByViewedAtDesc(
            Long resumeId,
            String viewSource,
            Pageable pageable
    );

    long countByResumeId(Long resumeId);

    long countByResumeIdAndViewerRole(Long resumeId, Role viewerRole);

    long countByResumeIdAndViewSource(Long resumeId, String viewSource);

    long countByOwnerUserId(Long ownerUserId);
    

    boolean existsByResumeIdAndViewerIdAndViewSource(
            Long resumeId,
            Long viewerId,
            String viewSource
    );
    
    
    @Query("""
    	    SELECT h.resumeId as resumeId, COUNT(h.id) as viewCount
    	    FROM ResumeViewHistory h
    	    WHERE h.resumeId IN :resumeIds
    	    GROUP BY h.resumeId
    	""")
    	List<ResumeViewCountProjection> countViewsByResumeIds(List<Long> resumeIds);
}