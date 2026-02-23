package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.jobapplicationdto.HiringFunnelDTO;
import com.lwd.jobportal.dto.jobdto.RecentJobDTO;
import com.lwd.jobportal.dto.recruiteradmindto.*;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruiterAdminDashboardService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public RecruiterAdminDashboardDTO getDashboard(Long companyId) {
        RecruiterAdminDashboardDTO dto = new RecruiterAdminDashboardDTO();

        // ---------- SUMMARY CARDS ----------
        dto.setTotalRecruitersInCompany(
                userRepository.countByCompanyIdAndRoleIn(
                        companyId,
                        List.of(Role.RECRUITER, Role.RECRUITER_ADMIN)
                )
        );
        dto.setTotalJobsPosted(jobRepository.countByCompanyId(companyId));
        dto.setActiveJobs(jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.OPEN));
        dto.setClosedJobs(jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.CLOSED));
        dto.setTotalApplications(applicationRepository.countByJobCompanyId(companyId));

        // ---------- RECRUITER PERFORMANCE ----------
        dto.setRecruiterPerformance(getRecruiterPerformance(companyId));

        // ---------- RECENT JOBS (last 5) ----------
        dto.setRecentJobs(
                jobRepository.findByCompanyIdOrderByCreatedAtDesc(
                        companyId,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
                ).stream()
                .map(this::mapToRecentJob)
                .collect(Collectors.toList())
        );

        // ---------- HIRING FUNNEL ----------
        dto.setHiringFunnel(getHiringFunnel(companyId));

        return dto;
    }

    // ==================== PRIVATE HELPERS ====================

    private List<RecruiterPerformanceDTO> getRecruiterPerformance(Long companyId) {
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
                .collect(Collectors.toList());
    }

    private RecentJobDTO mapToRecentJob(Job job) {
        RecentJobDTO dto = new RecentJobDTO();
        dto.setTitle(job.getTitle());
        dto.setPosted(job.getCreatedAt().toLocalDate().toString());
        dto.setStatus(job.getStatus().name());
        return dto;
    }

    private HiringFunnelDTO getHiringFunnel(Long companyId) {
        List<Object[]> results = applicationRepository.countByStatusForCompany(companyId);
        HiringFunnelDTO funnel = new HiringFunnelDTO();

        for (Object[] row : results) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            switch (status) {
                case APPLIED:
                    funnel.setApplied(count);
                    break;
                case SHORTLISTED:
                    funnel.setShortlisted(count);
                    break;
                case INTERVIEW_SCHEDULED:
                    funnel.setInterview(count);
                    break;
                case SELECTED:
                    funnel.setSelected(count);
                    break;
                case REJECTED:
                    funnel.setRejected(count);
                    break;
                default:
                    // ignore
            }
        }
        return funnel;
    }
}