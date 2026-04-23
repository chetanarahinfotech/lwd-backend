package com.lwd.jobportal.resume;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lwd.jobportal.entity.Resume;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.ResumeRepository;
import com.lwd.jobportal.util.ResumeFileUtils;
import com.lwd.jobportal.notification.CreateNotificationRequest;
import com.lwd.jobportal.notification.NotificationPriority;
import com.lwd.jobportal.notification.NotificationService;
import com.lwd.jobportal.notification.NotificationType;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

	private final Cloudinary cloudinary;
    private final ResumeRepository resumeRepository;
    private final ResumeViewHistoryService resumeViewHistoryService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ResumeResponse uploadResume(Long userId, MultipartFile file, boolean makeDefault) {
        ResumeFileUtils.validateResumeFile(file);

        try {
            String safeFileName = ResumeFileUtils.sanitizeFileName(file.getOriginalFilename());

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "lwd/resumes/" + userId,
                            "public_id", safeFileName.substring(0, safeFileName.lastIndexOf('.')),
                            "use_filename", true,
                            "unique_filename", false,
                            "overwrite", false
                    )
            );

            if (Boolean.TRUE.equals(makeDefault)) {
                clearExistingDefaultResume(userId);
            }

            Resume resume = Resume.builder()
                    .userId(userId)
                    .publicId(String.valueOf(uploadResult.get("public_id")))
                    .fileName(safeFileName)
                    .originalFileName(file.getOriginalFilename())
                    .fileUrl(String.valueOf(uploadResult.get("url")))
                    .secureUrl(String.valueOf(uploadResult.get("secure_url")))
                    .fileType(file.getContentType())
                    .fileFormat(uploadResult.get("format") != null ? String.valueOf(uploadResult.get("format")) : null)
                    .fileSize(file.getSize())
                    .version(uploadResult.get("version") != null ? String.valueOf(uploadResult.get("version")) : null)
                    .isDefault(makeDefault)
                    .parsed(false)
                    .deleted(false)
                    .uploadedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Resume saved = resumeRepository.save(resume);

            return mapToResponse(saved);

        } catch (IOException e) {
//            log.error("Failed to upload resume to Cloudinary for userId={}", userId, e);
            throw new RuntimeException("Failed to upload resume. Please try again.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getMyResumes(Long userId) {
        return resumeRepository.findByUserIdAndDeletedFalseOrderByUploadedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ResumeResponse setDefaultResume(Long userId, Long resumeId) {
        clearExistingDefaultResume(userId);

        Resume resume = resumeRepository.findByIdAndUserIdAndDeletedFalse(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        resume.setIsDefault(true);
        resume.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    public void softDeleteResume(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUserIdAndDeletedFalse(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        resume.setDeleted(true);
        resume.setIsDefault(false);
        resume.setUpdatedAt(LocalDateTime.now());
        resumeRepository.save(resume);
    }

    private void clearExistingDefaultResume(Long userId) {
        resumeRepository.findByUserIdAndIsDefaultTrueAndDeletedFalse(userId)
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    existing.setUpdatedAt(LocalDateTime.now());
                    resumeRepository.save(existing);
                });
    }

    private ResumeResponse mapToResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .userId(resume.getUserId())
                .fileName(resume.getFileName())
                .originalFileName(resume.getOriginalFileName())
                .fileUrl(resume.getFileUrl())
                .secureUrl(resume.getSecureUrl())
                .fileType(resume.getFileType())
                .fileFormat(resume.getFileFormat())
                .fileSize(resume.getFileSize())
                .isDefault(resume.getIsDefault())
                .parsed(resume.getParsed())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserIdAndDeletedFalse(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        return mapToResponse(resume);
    }


    @Override
    @Transactional
    public ResumeFileResponse viewResume(
            Long resumeId,
            Long currentUserId,
            Role currentRole,
            HttpServletRequest request,
            String viewSource,
            Long jobId,
            Long applicationId
    ) {

        Resume resume = resumeRepository.findById(resumeId)
                .filter(r -> !r.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        boolean isOwner = resume.getUserId().equals(currentUserId);

        boolean isRecruiter = currentRole == Role.RECRUITER
                || currentRole == Role.COMPANY_ADMIN
                || currentRole == Role.ADMIN;

        if (!isOwner && !isRecruiter) {
            throw new AccessDeniedException("You are not allowed to view this resume");
        }

        boolean alreadyViewed = resumeViewHistoryService.exists(
                resumeId,
                currentUserId,
                viewSource
        );

        if (!alreadyViewed) {

            resume.setViewCount(
                    resume.getViewCount() == null ? 1 : resume.getViewCount() + 1
            );
            resumeRepository.save(resume);

            resumeViewHistoryService.logResumeView(
                    resumeId,
                    resume.getUserId(),
                    currentUserId,
                    currentRole,
                    jobId,
                    applicationId,
                    viewSource,
                    request
            );

            // ✅ send notification only for non-owner viewer
            if (!isOwner) {
                sendResumeViewedNotification(
                        resume,
                        currentUserId,
                        currentRole,
                        viewSource,
                        applicationId,
                        jobId
                );
            }
        }

        return ResumeFileResponse.builder()
                .resumeId(resume.getId())
                .fileName(resume.getOriginalFileName())
                .fileUrl(resume.getSecureUrl())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .viewCount(resume.getViewCount())
                .build();
    } 
    
    public Resume getResumeByUserId(Long userId) {
        return resumeRepository
                .findByUserIdAndIsDefaultTrueAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found for user"));
    }

    @Override
    @Transactional
    public ResumeFileResponse downloadResume(
            Long resumeId,
            Long currentUserId,
            Role currentRole,
            HttpServletRequest request
    ) {
        Resume resume = resumeRepository.findById(resumeId)
                .filter(r -> !r.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        boolean isOwner = resume.getUserId().equals(currentUserId);

        boolean isRecruiter = currentRole == Role.RECRUITER
                || currentRole == Role.COMPANY_ADMIN
                || currentRole == Role.ADMIN;

        if (!isOwner && !isRecruiter) {
            throw new AccessDeniedException("You are not allowed to download this resume");
        }

        // optional: log separate download history later
        if (!isOwner) {
            sendResumeDownloadedNotification(resume, currentUserId, currentRole);
        }

        return ResumeFileResponse.builder()
                .resumeId(resume.getId())
                .fileName(resume.getOriginalFileName())
                .fileUrl(resume.getSecureUrl())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .viewCount(resume.getViewCount())
                .build();
    }
    
    private void sendResumeViewedNotification(
            Resume resume,
            Long viewerUserId,
            Role viewerRole,
            String viewSource,
            Long applicationId,
            Long jobId
    ) {
        String viewerRoleText = viewerRole != null
                ? viewerRole.name().replace("_", " ")
                : "User";

        String sourceText = (viewSource != null && !viewSource.isBlank())
                ? " via " + viewSource
                : "";

        String title = "Your resume was viewed";
        String message = "A " + viewerRoleText + sourceText + " viewed your resume.";

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(resume.getUserId()) // resume owner
                .type(NotificationType.RESUME_VIEWED)
                .priority(NotificationPriority.LOW)
                .title(title)
                .message(message)
                .actionUrl("/profile")
                .referenceId(resume.getId())
                .referenceType("RESUME")
                .build();

        notificationService.createNotification(request, viewerUserId);
    }
    
    
    private void sendResumeDownloadedNotification(
            Resume resume,
            Long viewerUserId,
            Role viewerRole
    ) {
        String viewerRoleText = viewerRole != null
                ? viewerRole.name().replace("_", " ")
                : "User";

        String title = "Your resume was downloaded";
        String message = "A " + viewerRoleText + " downloaded your resume.";

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(resume.getUserId())
                .type(NotificationType.RESUME_DOWNLOADED)
                .priority(NotificationPriority.MEDIUM)
                .title(title)
                .message(message)
                .actionUrl("/profile")
                .referenceId(resume.getId())
                .referenceType("RESUME")
                .build();

        notificationService.createNotification(request, viewerUserId);
    }
    
   
}