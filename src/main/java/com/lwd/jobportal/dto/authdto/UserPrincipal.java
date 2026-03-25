package com.lwd.jobportal.dto.authdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UserPrincipal {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Invalid email format")
    private String email;
}