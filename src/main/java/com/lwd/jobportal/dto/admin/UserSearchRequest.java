package com.lwd.jobportal.dto.admin;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import lombok.Data;

@Data
public class UserSearchRequest {
    private String keyword;

    private Role role;
    private UserStatus status;

    private Boolean isActive;
    private Boolean emailVerified;
    private Boolean locked;
}