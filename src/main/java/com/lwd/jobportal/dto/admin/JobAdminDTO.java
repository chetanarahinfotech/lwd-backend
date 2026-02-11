package com.lwd.jobportal.dto.admin;

import com.lwd.jobportal.enums.JobStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAdminDTO {
    private Long id;
    private String title;
    private String location;
    private JobStatus status;
}
