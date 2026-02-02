package com.lwd.jobportal.userdto;

import com.lwd.jobportal.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
