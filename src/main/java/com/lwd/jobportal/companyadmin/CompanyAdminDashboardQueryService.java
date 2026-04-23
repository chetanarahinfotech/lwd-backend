package com.lwd.jobportal.companyadmin;

import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.job.RecentJobDTO;
import com.lwd.jobportal.jobapplication.HiringFunnelDTO;
import com.lwd.jobportal.recruiter.RecruiterPerformanceDTO;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyAdminDashboardQueryService {

    private static final List<Role> RECRUITER_ROLES =
            List.of(Role.RECRUITER, Role.COMPANY_ADMIN);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public CompanyAdminSummaryDTO getSummary(Long companyId) {
        return CompanyAdminSummaryDTO.builder()
                .totalRecruitersInCompany(
                        userRepository.countByCompanyIdAndRoleIn(companyId, RECRUITER_ROLES)
                )
                .totalJobsPosted(jobRepository.countByCompanyId(companyId))
                .activeJobs(jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.OPEN))
                .closedJobs(jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.CLOSED))
                .totalApplications(applicationRepository.countByJobCompanyId(companyId))
                .build();
    }

    public List<RecruiterPerformanceDTO> getRecruiterPerformance(Long companyId) {
        return userRepository.findRecruiterPerformanceByCompanyId(companyId, RECRUITER_ROLES);
    }

    public List<RecentJobDTO> getRecentJobs(Long companyId, int size) {
        return jobRepository.findRecentJobsByCompanyId(
                companyId,
                PageRequest.of(0, size)
        );
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
}