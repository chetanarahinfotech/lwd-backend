package com.lwd.jobportal.dto.resume;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class ResumeFileResponse {

	private Long resumeId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private Resource resource;
    private String fileUrl;
    private String fileType;
    private Long viewCount;
}