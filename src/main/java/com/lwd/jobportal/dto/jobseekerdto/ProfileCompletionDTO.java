package com.lwd.jobportal.dto.jobseekerdto;

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
