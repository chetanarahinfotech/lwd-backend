package com.lwd.jobportal.dto.jobapplicationdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyApplication {
    private LocalDate day;
    private long count;
}