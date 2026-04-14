package com.lwd.jobportal.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

public final class ResumeFileUtils {

    private ResumeFileUtils() {}

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx"
    );

    public static void validateResumeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PDF, DOC, and DOCX files are allowed");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension");
        }
    }

    public static String sanitizeFileName(String originalName) {
        String ext = getExtension(originalName);
        String base = originalName == null ? "resume" :
                originalName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

        String withoutExt = base.contains(".")
                ? base.substring(0, base.lastIndexOf('.'))
                : base;

        return withoutExt + "_" + UUID.randomUUID() + "." + ext;
    }

    public static String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}