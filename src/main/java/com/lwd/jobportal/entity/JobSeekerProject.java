package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity
@Table(
    name = "job_seeker_projects",
    indexes = {
        @Index(name = "idx_project_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 User reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String projectTitle;

    @Column(length = 1200)
    private String description;

    private String technologiesUsed;

    private String projectUrl;
    private String githubUrl;

    private LocalDate startDate;
    private LocalDate endDate;

    // 🔥 ATS fields
    private Integer teamSize;

    private String role; // Developer / Backend / Fullstack
}
