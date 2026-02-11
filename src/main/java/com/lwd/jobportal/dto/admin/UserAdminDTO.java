package com.lwd.jobportal.dto.admin;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAdminDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserStatus status;
    private Boolean isActive;
}
