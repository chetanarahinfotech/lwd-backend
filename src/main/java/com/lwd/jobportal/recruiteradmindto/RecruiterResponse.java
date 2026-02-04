package com.lwd.jobportal.recruiteradmindto;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RecruiterResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
