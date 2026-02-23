package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.jobapplicationdto.RecentApplicationDTO;
import com.lwd.jobportal.dto.jobdto.JobStatsDTO;
import com.lwd.jobportal.dto.recruiterdto.*;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruiterDashboardService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public RecruiterDashboardDTO getDashboard(Long recruiterId) {
        RecruiterDashboardDTO dto = new RecruiterDashboardDTO();

        // ---------- SUMMARY CARDS ----------
        dto.setMyPostedJobs(jobRepository.countByCreatedById(recruiterId));
        dto.setMyActiveJobs(jobRepository.countByCreatedByIdAndStatus(recruiterId, JobStatus.OPEN)); // OPEN not ACTIVE
        dto.setTotalApplications(applicationRepository.countByJobCreatedById(recruiterId));
        dto.setInterviewsScheduled(
                applicationRepository.countByJobCreatedByIdAndStatus(recruiterId, ApplicationStatus.INTERVIEW_SCHEDULED)
        );
        dto.setShortlistedCandidates(
                applicationRepository.countByJobCreatedByIdAndStatus(recruiterId, ApplicationStatus.SHORTLISTED)
        );

        // ---------- PER JOB STATS ----------
        dto.setPerJobStats(getPerJobStats(recruiterId));

        // ---------- RECENT APPLICATIONS (last 5) ----------
        dto.setRecentApplications(
                applicationRepository.findTop5ByJobCreatedByIdOrderByAppliedAtDesc(recruiterId)
                        .stream()
                        .map(this::mapToRecentApplication)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    // ==================== PRIVATE HELPERS ====================

    private List<JobStatsDTO> getPerJobStats(Long recruiterId) {
        List<Job> jobs = jobRepository.findByCreatedById(recruiterId);
        return jobs.stream()
                .map(job -> {
                    JobStatsDTO stats = new JobStatsDTO();
                    stats.setJobTitle(job.getTitle());

                    long totalApps = applicationRepository.countByJobId(job.getId());
                    stats.setApplications(totalApps);

                    long shortlisted = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.SHORTLISTED);
                    stats.setShortlisted(shortlisted);

                    long rejected = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.REJECTED);
                    stats.setRejected(rejected);

                    // Pending = APPLIED + INTERVIEW_SCHEDULED (not shortlisted/rejected/selected/hired)
                    long pending = applicationRepository.countByJobIdAndStatusIn(
                            job.getId(),
                            List.of(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW_SCHEDULED)
                    );
                    stats.setPending(pending);

                    // Optional: interview count separately
                    long interview = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.INTERVIEW_SCHEDULED);
                    stats.setInterview(interview);

                    return stats;
                })
                .collect(Collectors.toList());
    }

    private RecentApplicationDTO mapToRecentApplication(JobApplication app) {
        RecentApplicationDTO dto = new RecentApplicationDTO();

        // Candidate name: prefer fullName (external) else jobSeeker.name
        if (app.getFullName() != null && !app.getFullName().isEmpty()) {
            dto.setCandidateName(app.getFullName());
        } else if (app.getJobSeeker() != null) {
            dto.setCandidateName(app.getJobSeeker().getName());
        } else {
            dto.setCandidateName("Unknown");
        }

        dto.setJobTitle(app.getJob().getTitle());
        dto.setAppliedDate(app.getAppliedAt().toLocalDate().toString());
        dto.setStatus(app.getStatus().name());
        dto.setApplicationSource(app.getApplicationSource() != null ? app.getApplicationSource().name() : "PORTAL");

        return dto;
    }
}