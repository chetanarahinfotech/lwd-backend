package com.lwd.jobportal.resume;

import com.lwd.jobportal.entity.Resume;
import com.lwd.jobportal.enums.Role;

public interface ResumeAccessService {
    void validateViewPermission(Long currentUserId, Role role, Resume resume);
}