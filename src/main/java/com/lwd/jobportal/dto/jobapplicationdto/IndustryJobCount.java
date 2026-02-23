package com.lwd.jobportal.dto.jobapplicationdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndustryJobCount {
    private String industry;
    private long count;
}