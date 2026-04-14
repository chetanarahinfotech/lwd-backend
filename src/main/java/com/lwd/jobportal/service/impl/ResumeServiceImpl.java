package com.lwd.jobportal.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lwd.jobportal.dto.resume.ResumeFileResponse;
import com.lwd.jobportal.dto.resume.ResumeResponse;
import com.lwd.jobportal.entity.Resume;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.ResumeRepository;
import com.lwd.jobportal.service.ResumeService;
import com.lwd.jobportal.service.ResumeViewHistoryService;
import com.lwd.jobportal.util.ResumeFileUtils;

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
            Long jobId,          // 👈 ADD
            Long applicationId   // 👈 ADD
    ) {

        Resume resume = resumeRepository.findById(resumeId)
                .filter(r -> !r.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        // 🔐 SECURITY CHECK
        boolean isOwner = resume.getUserId().equals(currentUserId);

        boolean isRecruiter = currentRole == Role.RECRUITER
                || currentRole == Role.RECRUITER_ADMIN
                || currentRole == Role.ADMIN;

        if (!isOwner && !isRecruiter) {
            throw new AccessDeniedException("You are not allowed to view this resume");
        }

        // ===============================
        // 🔥 SMART UNIQUE VIEW LOGIC
        // ===============================
        boolean alreadyViewed =
                resumeViewHistoryService.exists(
                        resumeId,
                        currentUserId,
                        viewSource
                );

        if (!alreadyViewed) {

            // ✅ increment only once per source
            resume.setViewCount(
                    resume.getViewCount() == null ? 1 : resume.getViewCount() + 1
            );
            resumeRepository.save(resume);

            // ===============================
            // ✅ SAVE FULL HISTORY
            // ===============================
            resumeViewHistoryService.logResumeView(
                    resumeId,
                    resume.getUserId(),     // ownerUserId
                    currentUserId,          // viewerId
                    currentRole,            // viewerRole
                    jobId,
                    applicationId,
                    viewSource,
                    request
            );
        }

        // ===============================
        // 🔗 RETURN RESPONSE
        // ===============================
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
    public ResumeFileResponse downloadResume(
            Long resumeId,
            Long currentUserId,
            Role currentRole,
            HttpServletRequest request
    ) {
        throw new UnsupportedOperationException("Implement secure resume download logic");
    }
    
   
}