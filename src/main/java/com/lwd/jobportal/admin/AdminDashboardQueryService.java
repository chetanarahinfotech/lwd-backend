package com.lwd.jobportal.admin;

import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.job.RecentJobDTO;
import com.lwd.jobportal.jobapplication.DailyApplication;
import com.lwd.jobportal.jobapplication.RecentApplicationDTO;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.user.RecentUserDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardQueryService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public AdminSummaryDTO getSummary() {
        return AdminSummaryDTO.builder()
                .totalUsers(userRepository.count())
                .totalCompanies(companyRepository.count())
                .totalJobs(jobRepository.count())
                .totalApplications(applicationRepository.count())
                .totalRecruiters(
                        userRepository.countByRole(Role.RECRUITER)
                                + userRepository.countByRole(Role.COMPANY_ADMIN)
                )
                .activeJobs(jobRepository.countByStatus(JobStatus.OPEN))
                .build();
    }

    public AdminGrowthDTO getGrowth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.minusMonths(1);
        LocalDateTime weekStart = now.minusWeeks(1);

        return AdminGrowthDTO.builder()
                .usersThisMonth(userRepository.countByCreatedAtBetween(monthStart, now))
                .jobsThisMonth(jobRepository.countByCreatedAtBetween(monthStart, now))
                .applicationsThisWeek(applicationRepository.countByAppliedAtBetween(weekStart, now))
                .newCompaniesThisMonth(companyRepository.countByCreatedAtBetween(monthStart, now))
                .build();
    }

    public List<RecentUserDTO> getRecentUsers(int size) {
        return userRepository.findRecentUsers(size)
                .stream()
                .map(this::mapToRecentUser)
                .toList();
    }

    public List<RecentJobDTO> getRecentJobs(int size) {
        return jobRepository.findRecentJobs(size)
                .stream()
                .map(this::mapToRecentJob)
                .toList();
    }

    public List<RecentApplicationDTO> getRecentApplications(int size) {
        return applicationRepository.findRecentApplications(size)
                .stream()
                .map(this::mapToRecentApplication)
                .toList();
    }

    public Map<String, Long> getJobsPerIndustry() {
        List<Object[]> results = jobRepository.countJobsPerIndustry();
        Map<String, Long> map = new LinkedHashMap<>();

        for (Object[] row : results) {
            map.put((String) row[0], ((Number) row[1]).longValue());
        }

        return map;
    }

    public List<DailyApplication> getApplicationsTrend() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        return applicationRepository.countApplicationsPerDay(weekAgo)
                .stream()
                .map(row -> new DailyApplication(((Date) row[0]).toLocalDate(), ((Number) row[1]).longValue()))
                .toList();
    }

    public Map<String, Long> getUsersByRole() {
        Map<String, Long> map = new LinkedHashMap<>();

        for (Role role : Role.values()) {
            long count = userRepository.countByRole(role);
            if (count > 0) {
                map.put(role.name(), count);
            }
        }

        return map;
    }

    public SystemHealthDTO getSystemHealth() {
        LocalDateTime now = LocalDateTime.now();

        return SystemHealthDTO.builder()
                .activeRecruiters(
                        userRepository.countByRole(Role.RECRUITER)
                                + userRepository.countByRole(Role.COMPANY_ADMIN)
                )
                .jobsExpiringSoon((long) jobRepository.findJobsExpiringSoon(now, now.plusDays(7)).size())
                .jobsWithoutApplications((long) jobRepository.findJobsWithoutApplications().size())
                .pendingApprovals(0L)
                .build();
    }

    private RecentUserDTO mapToRecentUser(User user) {
        return RecentUserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .joined(user.getCreatedAt().toLocalDate().toString())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .build();
    }

    private RecentJobDTO mapToRecentJob(Job job) {
        String companyName = null;

        if (job.getCompany() != null) {
            // Avoid triggering lazy load if possible
            companyName = job.getCompany().getCompanyName();
        }

        return RecentJobDTO.builder()
                .title(job.getTitle())
                .companyName(companyName)
                .location(job.getLocation())
                .industry(job.getIndustry())
                .jobStatus(job.getStatus())
                .createdAt(job.getCreatedAt())
                .build();
    }
    private RecentApplicationDTO mapToRecentApplication(JobApplication app) {
        String candidateName = "Unknown";

        if (app.getFullName() != null && !app.getFullName().isBlank()) {
            candidateName = app.getFullName();
        } else if (app.getJobSeeker() != null) {
            candidateName = app.getJobSeeker().getName();
        }

        return RecentApplicationDTO.builder()
                .candidateName(candidateName)
                .jobTitle(app.getJob() != null ? app.getJob().getTitle() : null)
                .appliedDate(app.getAppliedAt().toLocalDate().toString())
                .status(app.getStatus().name())
                .applicationSource(app.getApplicationSource().name())
                .build();
    }
}