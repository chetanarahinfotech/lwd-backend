package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity
@Table(
    name = "job_seeker_internships",
    indexes = {
        @Index(name = "idx_internship_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerInternship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 User reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String companyName;
    private String role;

    private LocalDate startDate;
    private LocalDate endDate;

    private String location;

    @Column(length = 1000)
    private String description;

    // 🔥 ATS fields
    private String skills;

    private Double stipend;

    private String employmentType; // REMOTE / ONSITE / HYBRID
}
