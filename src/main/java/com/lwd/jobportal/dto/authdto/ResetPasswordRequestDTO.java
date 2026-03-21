package com.lwd.jobportal.dto.authdto;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {

    private String token;
    private String newPassword;

}
