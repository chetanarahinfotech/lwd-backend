package com.lwd.jobportal.dto.jobapplicationdto;

import lombok.Data;

@Data
public class HiringFunnelDTO {
    private long applied;
    private long shortlisted;
    private long interview;
    private long selected;
    private long rejected;
}