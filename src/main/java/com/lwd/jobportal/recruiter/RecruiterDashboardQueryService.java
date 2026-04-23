package com.lwd.jobportal.recruiter;

import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.job.JobStatsDTO;
import com.lwd.jobportal.jobapplication.RecentApplicationDTO;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruiterDashboardQueryService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public RecruiterSummaryDTO getSummary(Long recruiterId) {
        return RecruiterSummaryDTO.builder()
                .myPostedJobs(jobRepository.countByCreatedById(recruiterId))
                .myActiveJobs(jobRepository.countByCreatedByIdAndStatus(recruiterId, JobStatus.OPEN))
                .totalApplications(applicationRepository.countByJobCreatedById(recruiterId))
                .interviewsScheduled(
                        applicationRepository.countByJobCreatedByIdAndStatus(
                                recruiterId,
                                ApplicationStatus.INTERVIEW_SCHEDULED
                        )
                )
                .shortlistedCandidates(
                        applicationRepository.countByJobCreatedByIdAndStatus(
                                recruiterId,
                                ApplicationStatus.SHORTLISTED
                        )
                )
                .build();
    }

    public List<JobStatsDTO> getPerJobStats(Long recruiterId) {
        List<Job> jobs = jobRepository.findByCreatedById(recruiterId);

        if (jobs.isEmpty()) {
            return List.of();
        }

        List<Long> jobIds = jobs.stream()
                .map(Job::getId)
                .toList();

        List<Object[]> statsRows = applicationRepository.getJobStatsForJobIds(jobIds);

        Map<Long, JobStatsAggregate> statsMap = new HashMap<>();

        for (Object[] row : statsRows) {
            Long jobId = ((Number) row[0]).longValue();
            long totalApplications = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            long shortlisted = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            long rejected = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            long pending = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            long interview = row[5] != null ? ((Number) row[5]).longValue() : 0L;

            statsMap.put(jobId, new JobStatsAggregate(
                    totalApplications,
                    shortlisted,
                    rejected,
                    pending,
                    interview
            ));
        }

        return jobs.stream()
                .map(job -> {
                    JobStatsAggregate stats = statsMap.getOrDefault(
                            job.getId(),
                            JobStatsAggregate.ZERO
                    );

                    return JobStatsDTO.builder()
                            .jobTitle(job.getTitle())
                            .applications(stats.totalApplications())
                            .shortlisted(stats.shortlisted())
                            .rejected(stats.rejected())
                            .pending(stats.pending())
                            .interview(stats.interview())
                            .build();
                })
                .toList();
    }

    public List<RecentApplicationDTO> getRecentApplications(Long recruiterId, int size) {
        return applicationRepository
                .findRecentApplicationsByRecruiterId(recruiterId, PageRequest.of(0, size))
                .stream()
                .map(this::mapToRecentApplication)
                .toList();
    }

    private RecentApplicationDTO mapToRecentApplication(JobApplication app) {
        String candidateName;

        if (app.getFullName() != null && !app.getFullName().isBlank()) {
            candidateName = app.getFullName();
        } else if (app.getJobSeeker() != null) {
            candidateName = app.getJobSeeker().getName();
        } else {
            candidateName = "Unknown";
        }

        return RecentApplicationDTO.builder()
                .applicationId(app.getId())
                .candidateName(candidateName)
                .jobTitle(app.getJob() != null ? app.getJob().getTitle() : null)
                .appliedDate(
                        app.getAppliedAt() != null
                                ? app.getAppliedAt().toLocalDate().toString()
                                : null
                )
                .status(app.getStatus() != null ? app.getStatus().name() : null)
                .applicationSource(
                        app.getApplicationSource() != null
                                ? app.getApplicationSource().name()
                                : "PORTAL"
                )
                .build();
    }

    private record JobStatsAggregate(
            long totalApplications,
            long shortlisted,
            long rejected,
            long pending,
            long interview
    ) {
        private static final JobStatsAggregate ZERO =
                new JobStatsAggregate(0L, 0L, 0L, 0L, 0L);
    }
}