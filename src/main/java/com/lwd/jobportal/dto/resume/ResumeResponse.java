package com.lwd.jobportal.dto.resume;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeResponse {

    private Long id;
    private Long userId;
    private String publicId;
    private String fileName;
    private String originalFileName;
    private String fileUrl;
    private String secureUrl;
    private String fileType;
    private String fileFormat;
    private Long fileSize;
    private String version;
    private Boolean isDefault;
    private Boolean parsed;
    private Boolean deleted;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

    private String storageProvider;
    private String resourceType;
    private String accessLevel;
    private String mimeType;

    private Long downloadCount;
    private Long viewCount;
}