package com.lwd.jobportal.companyadmin;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserApprovalResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private Boolean isActive;
    private Long companyId;
    private String companyName;
}