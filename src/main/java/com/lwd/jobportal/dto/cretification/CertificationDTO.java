package com.lwd.jobportal.dto.cretification;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationDTO {

    private Long id;
    private Long userId;

    private String certificateName;
    private String issuingOrganization;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    private String credentialId;
    private String credentialUrl;

    private String skillTag;
    private String certificateFile;
}
