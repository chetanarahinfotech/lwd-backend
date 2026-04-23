package com.lwd.jobportal.admin;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import lombok.Data;

@Data
public class UserSearchRequestDTO {
    private String keyword;

    private Role role;
    private UserStatus status;

    private Boolean isActive;
    private Boolean emailVerified;
    private Boolean locked;
}