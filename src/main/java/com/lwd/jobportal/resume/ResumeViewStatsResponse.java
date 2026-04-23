package com.lwd.jobportal.resume;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumeViewStatsResponse {

    private Long resumeId;
    private Long totalViews;
    private Long recruiterViews;
    private Long adminViews;
    private Long profileViews;
    private Long applicationViews;
    private Long searchViews;
}