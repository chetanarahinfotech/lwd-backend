package com.lwd.jobportal.jobseeker;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileCompletionDTO {

    private int percentage;

    private List<String> missingSections;

}
