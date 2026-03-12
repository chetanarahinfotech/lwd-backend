package com.lwd.jobportal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDTO {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String companyName;
}
