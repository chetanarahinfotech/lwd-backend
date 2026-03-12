package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "job_seeker_education",
    indexes = {
        @Index(name = "idx_education_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Mapping with user_id instead of job_seeker_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String degree;
    private String fieldOfStudy;
    private String institutionName;
    private String university;

    private LocalDate startDate;
    private LocalDate endDate;

    private Double percentage;
    private String grade;
}
