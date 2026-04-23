package com.lwd.jobportal.admin;

import com.lwd.jobportal.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

	@NotNull(message="Status is required")
    private UserStatus status;
}