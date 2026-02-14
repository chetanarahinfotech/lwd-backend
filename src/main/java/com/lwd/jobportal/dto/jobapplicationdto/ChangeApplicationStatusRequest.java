package com.lwd.jobportal.dto.jobapplicationdto;

import com.lwd.jobportal.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeApplicationStatusRequest {

    @NotNull
    private ApplicationStatus status;
}
