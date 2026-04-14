package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes", indexes = {
        @Index(name = "idx_resume_user_id", columnList = "user_id"),
        @Index(name = "idx_resume_default", columnList = "is_default"),
        @Index(name = "idx_resume_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "secure_url", nullable = false, length = 1000)
    private String secureUrl;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(name = "file_format", length = 20)
    private String fileFormat;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "version")
    private String version;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "parsed", nullable = false)
    @Builder.Default
    private Boolean parsed = false;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "storage_provider", length = 30)
    private String storageProvider; // CLOUDINARY

    @Column(name = "resource_type", length = 30)
    private String resourceType; // raw, image, auto

    @Column(name = "access_level", length = 30)
    private String accessLevel; // PRIVATE, RESTRICTED, PUBLIC

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "download_count")
    private Long downloadCount;

    @Column(name = "view_count")
    private Long viewCount;

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}