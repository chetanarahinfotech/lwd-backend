package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.lwd.jobportal.enums.NoticeStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "job_seeker_profiles",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")
    }
)
public class JobSeeker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Owning Side
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private NoticeStatus noticeStatus;

    private Boolean isServingNotice;

    private LocalDate lastWorkingDay;

    private Integer noticePeriod;

    private LocalDate availableFrom;

    private Boolean immediateJoiner;

    private String currentCompany;

    private Double currentCTC;

    private Double expectedCTC;

    private String currentLocation;

    private String preferredLocation;

    private Integer totalExperience;

    private String skills;

    @Column(length = 1000)
    private String resumeUrl;
}
