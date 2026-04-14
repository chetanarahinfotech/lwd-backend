package com.lwd.jobportal.service.impl;

import com.lwd.jobportal.dto.resume.ResumeViewHistoryResponse;
import com.lwd.jobportal.dto.resume.ResumeViewStatsResponse;
import com.lwd.jobportal.entity.ResumeViewHistory;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.repository.ResumeViewHistoryRepository;
import com.lwd.jobportal.service.ResumeViewHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ResumeViewHistoryServiceImpl implements ResumeViewHistoryService {

    private final ResumeViewHistoryRepository resumeViewHistoryRepository;

    @Override
    public void logResumeView(
            Long resumeId,
            Long ownerUserId,
            Long viewerId,
            Role viewerRole,
            Long jobId,
            Long applicationId,
            String viewSource,
            HttpServletRequest request
    ) {
        boolean alreadyViewedFromSameSource =
                resumeViewHistoryRepository.existsByResumeIdAndViewerIdAndViewSource(
                        resumeId,
                        viewerId,
                        viewSource
                );

        if (alreadyViewedFromSameSource) {
            return;
        }

        ResumeViewHistory history = ResumeViewHistory.builder()
                .resumeId(resumeId)
                .ownerUserId(ownerUserId)
                .viewerId(viewerId)
                .viewerRole(viewerRole)
                .jobId(jobId)
                .applicationId(applicationId)
                .viewSource(viewSource)
                .ipAddress(extractClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .viewedAt(LocalDateTime.now())
                .build();

        resumeViewHistoryRepository.save(history);
    }

    @Override
    public Page<ResumeViewHistoryResponse> getResumeViewHistory(Long resumeId, Pageable pageable) {
        return resumeViewHistoryRepository.findByResumeIdOrderByViewedAtDesc(resumeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ResumeViewHistoryResponse> getOwnerResumeViewHistory(Long ownerUserId, Pageable pageable) {
        return resumeViewHistoryRepository.findByOwnerUserIdOrderByViewedAtDesc(ownerUserId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public ResumeViewStatsResponse getResumeViewStats(Long resumeId) {
        long totalViews = resumeViewHistoryRepository.countByResumeId(resumeId);
        long recruiterViews = resumeViewHistoryRepository.countByResumeIdAndViewerRole(resumeId, Role.RECRUITER)
                + resumeViewHistoryRepository.countByResumeIdAndViewerRole(resumeId, Role.RECRUITER_ADMIN);
        long adminViews = resumeViewHistoryRepository.countByResumeIdAndViewerRole(resumeId, Role.ADMIN)
                + resumeViewHistoryRepository.countByResumeIdAndViewerRole(resumeId, Role.SUPER_ADMIN);
        long profileViews = resumeViewHistoryRepository.countByResumeIdAndViewSource(resumeId, "PROFILE");
        long applicationViews = resumeViewHistoryRepository.countByResumeIdAndViewSource(resumeId, "APPLICATION");
        long searchViews = resumeViewHistoryRepository.countByResumeIdAndViewSource(resumeId, "SEARCH");

        return ResumeViewStatsResponse.builder()
                .resumeId(resumeId)
                .totalViews(totalViews)
                .recruiterViews(recruiterViews)
                .adminViews(adminViews)
                .profileViews(profileViews)
                .applicationViews(applicationViews)
                .searchViews(searchViews)
                .build();
    }

    private ResumeViewHistoryResponse mapToResponse(ResumeViewHistory history) {
        return ResumeViewHistoryResponse.builder()
                .id(history.getId())
                .resumeId(history.getResumeId())
                .ownerUserId(history.getOwnerUserId())
                .viewerId(history.getViewerId())
                .viewerRole(history.getViewerRole())
                .jobId(history.getJobId())
                .applicationId(history.getApplicationId())
                .viewSource(history.getViewSource())
                .ipAddress(history.getIpAddress())
                .userAgent(history.getUserAgent())
                .viewedAt(history.getViewedAt())
                .build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }

        return Objects.toString(request.getRemoteAddr(), null);
    }
    
    @Override
    public boolean exists(Long resumeId, Long viewerId, String viewSource) {
        return resumeViewHistoryRepository
                .existsByResumeIdAndViewerIdAndViewSource(
                        resumeId,
                        viewerId,
                        viewSource
                );
    }
}