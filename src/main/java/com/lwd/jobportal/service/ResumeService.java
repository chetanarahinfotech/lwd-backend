package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.resume.ResumeFileResponse;
import com.lwd.jobportal.dto.resume.ResumeResponse;
import com.lwd.jobportal.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    ResumeResponse uploadResume(Long userId, MultipartFile file, boolean makeDefault);

    List<ResumeResponse> getMyResumes(Long userId);

    ResumeResponse getResumeById(Long resumeId, Long userId);

    ResumeResponse setDefaultResume(Long userId, Long resumeId);

    void softDeleteResume(Long userId, Long resumeId);


    ResumeFileResponse downloadResume(
            Long resumeId,
            Long currentUserId,
            Role currentRole,
            HttpServletRequest request
    );

	ResumeFileResponse viewResume(
			Long resumeId, 
			Long currentUserId, 
			Role currentRole, 
			HttpServletRequest request,
			String viewSource, 
			Long jobId, 
			Long applicationId);
}