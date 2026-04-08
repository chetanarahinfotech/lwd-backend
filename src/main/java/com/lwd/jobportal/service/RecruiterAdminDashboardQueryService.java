package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.jobapplicationdto.HiringFunnelDTO;
import com.lwd.jobportal.dto.jobdto.RecentJobDTO;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterAdminSummaryDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterPerformanceDTO;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruiterAdminDashboardQueryService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public RecruiterAdminSummaryDTO getSummary(Long companyId) {
        return RecruiterAdminSummaryDTO.builder()
                .totalRecruitersInCompany(
                        userRepository.countByCompanyIdAndRoleIn(
                                companyId,
                                List.of(Role.RECRUITER, Role.RECRUITER_ADMIN)
                        )
                )
                .totalJobsPosted(jobRepository.countByCompanyId(companyId))
                .activeJobs(jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.OPEN))
                .closedJobs(jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.CLOSED))
                .totalApplications(applicationRepository.countByJobCompanyId(companyId))
                .build();
    }

    public List<RecruiterPerformanceDTO> getRecruiterPerformance(Long companyId) {
        List<User> recruiters = userRepository.findByCompanyIdAndRoleIn(
                companyId,
                List.of(Role.RECRUITER, Role.RECRUITER_ADMIN)
        );

        return recruiters.stream()
                .map(recruiter -> {
                    RecruiterPerformanceDTO perf = new RecruiterPerformanceDTO();
                    perf.setRecruiterName(recruiter.getName());
                    perf.setJobsPosted(jobRepository.countByCreatedById(recruiter.getId()));
                    perf.setApplicationsReceived(applicationRepository.countByJobCreatedById(recruiter.getId()));
                    perf.setActiveJobs(jobRepository.countByCreatedByIdAndStatus(recruiter.getId(), JobStatus.OPEN));
                    return perf;
                })
                .toList();
    }

    public List<RecentJobDTO> getRecentJobs(Long companyId, int size) {
        return jobRepository.findByCompanyIdOrderByCreatedAtDesc(
                        companyId,
                        PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                ).stream()
                .map(this::mapToRecentJob)
                .toList();
    }

    public HiringFunnelDTO getHiringFunnel(Long companyId) {
        List<Object[]> results = applicationRepository.countByStatusForCompany(companyId);
        HiringFunnelDTO funnel = new HiringFunnelDTO();

        for (Object[] row : results) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            long count = ((Number) row[1]).longValue();

            switch (status) {
                case APPLIED -> funnel.setApplied(count);
                case SHORTLISTED -> funnel.setShortlisted(count);
                case INTERVIEW_SCHEDULED -> funnel.setInterview(count);
                case SELECTED -> funnel.setSelected(count);
                case REJECTED -> funnel.setRejected(count);
                default -> {
                }
            }
        }

        return funnel;
    }

    private RecentJobDTO mapToRecentJob(Job job) {
        return RecentJobDTO.builder()
                .title(job.getTitle())
                .companyName(job.getCompany() != null ? job.getCompany().getCompanyName() : null)
                .location(job.getLocation())
                .industry(job.getIndustry())
                .posted(job.getCreatedAt().toLocalDate().toString())
                .status(job.getStatus().name())
                .build();
    }
}