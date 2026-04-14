package com.lwd.jobportal.dto.resume;

import com.lwd.jobportal.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeViewHistoryResponse {

    private Long id;
    private Long resumeId;
    private Long ownerUserId;
    private Long viewerId;
    private Role viewerRole;

    private Long jobId;
    private Long applicationId;

    private String viewSource;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime viewedAt;
}