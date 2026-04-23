package com.lwd.jobportal.auth.dto;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrincipal {
    private Long userId;
    private String email;
    private Role role;
    private UserStatus status;
    private boolean emailVerified;
    private Long companyId;
}