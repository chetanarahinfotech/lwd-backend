package com.lwd.jobportal.dto.jobdto;

import com.lwd.jobportal.enums.ApplicationSource;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;
import lombok.Data;

@Data
public class JobSearchRequest {

    private String keyword;

    private String location;
    private String industry;
    private String roleCategory;
    private String department;
    private String workplaceType;
    private String skills;

    private JobType jobType;
    private JobStatus status;
    private ApplicationSource applicationSource;
    private NoticeStatus noticePreference;

    private Integer minExperience;
    private Integer maxExperience;

    private Double minSalary;
    private Double maxSalary;

    private Boolean lwdPreferred;
}