package com.lwd.jobportal.jobseeker;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSkillsRequest {
    private List<String> skills;
}
