package com.lwd.jobportal.dto.authdto;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RegisterResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
