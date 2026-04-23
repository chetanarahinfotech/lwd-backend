package com.lwd.jobportal.resume;

import com.lwd.jobportal.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResumeViewHistoryService {

    void logResumeView(
            Long resumeId,
            Long ownerUserId,
            Long viewerId,
            Role viewerRole,
            Long jobId,
            Long applicationId,
            String viewSource,
            HttpServletRequest request
    );

    Page<ResumeViewHistoryResponse> getResumeViewHistory(Long resumeId, Pageable pageable);

    Page<ResumeViewHistoryResponse> getOwnerResumeViewHistory(Long ownerUserId, Pageable pageable);

    ResumeViewStatsResponse getResumeViewStats(Long resumeId);

	boolean exists(Long resumeId, Long currentUserId, String viewSource);

}