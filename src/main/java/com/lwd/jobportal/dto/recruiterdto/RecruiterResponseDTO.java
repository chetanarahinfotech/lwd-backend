package com.lwd.jobportal.dto.recruiterdto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecruiterResponseDTO {

    private Long id;
    private Long userId;

    private String designation;
    private Integer experience;
    private String location;
    private String phone;
    private String linkedinUrl;
    private String about;

    private Integer profileCompletion;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
