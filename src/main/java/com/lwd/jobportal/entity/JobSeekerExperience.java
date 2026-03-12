package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "job_seeker_experience",
    indexes = {
        @Index(name = "idx_experience_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Mapping with user_id instead of job_seeker_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String companyName;
    private String jobTitle;
    private String employmentType; // Full-time, Internship etc

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean currentlyWorking;

    @Column(length = 2000)
    private String jobDescription;

    private String location;
}
