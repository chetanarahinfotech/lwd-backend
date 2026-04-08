package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.jobapplicationdto.RecentApplicationDTO;
import com.lwd.jobportal.dto.jobdto.JobStatsDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterSummaryDTO;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        return jobs.stream()
                .map(job -> {
                    long totalApps = applicationRepository.countByJobId(job.getId());

                    long shortlisted = applicationRepository.countByJobIdAndStatus(
                            job.getId(),
                            ApplicationStatus.SHORTLISTED
                    );

                    long rejected = applicationRepository.countByJobIdAndStatus(
                            job.getId(),
                            ApplicationStatus.REJECTED
                    );

                    long pending = applicationRepository.countByJobIdAndStatusIn(
                            job.getId(),
                            List.of(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW_SCHEDULED)
                    );

                    long interview = applicationRepository.countByJobIdAndStatus(
                            job.getId(),
                            ApplicationStatus.INTERVIEW_SCHEDULED
                    );

                    return JobStatsDTO.builder()
                            .jobTitle(job.getTitle())
                            .applications(totalApps)
                            .shortlisted(shortlisted)
                            .rejected(rejected)
                            .pending(pending)
                            .interview(interview)
                            .build();
                })
                .toList();
    }

    public List<RecentApplicationDTO> getRecentApplications(Long recruiterId, int size) {
        return applicationRepository
                .findRecentApplicationsByRecruiterId(recruiterId, org.springframework.data.domain.PageRequest.of(0, size))
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
}