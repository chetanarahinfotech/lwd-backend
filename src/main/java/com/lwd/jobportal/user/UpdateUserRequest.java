package com.lwd.jobportal.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String name;
    private String phone;
    private Boolean isActive;
}
