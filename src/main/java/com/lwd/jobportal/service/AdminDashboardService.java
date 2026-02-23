package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.admin.AdminDashboardDTO;
import com.lwd.jobportal.dto.jobapplicationdto.DailyApplication;
import com.lwd.jobportal.dto.jobapplicationdto.RecentApplicationDTO;
import com.lwd.jobportal.dto.jobdto.RecentJobDTO;
import com.lwd.jobportal.dto.userdto.RecentUserDTO;
import com.lwd.jobportal.entity.*;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public AdminDashboardDTO getAdminDashboard() {
        AdminDashboardDTO dto = new AdminDashboardDTO();

        // ---------- KPI CARDS ----------
        dto.setTotalUsers(userRepository.count());
        dto.setTotalCompanies(companyRepository.count());
        dto.setTotalJobs(jobRepository.count());
        dto.setTotalApplications(applicationRepository.count());
        dto.setTotalRecruiters(
                userRepository.countByRole(Role.RECRUITER) +
                userRepository.countByRole(Role.RECRUITER_ADMIN)
        );
        dto.setActiveJobs(jobRepository.countByStatus(JobStatus.OPEN));   // OPEN, not ACTIVE

        // ---------- GROWTH METRICS ----------
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.minusMonths(1);
        LocalDateTime weekStart = now.minusWeeks(1);

        dto.setUsersThisMonth(userRepository.countByCreatedAtBetween(monthStart, now));
        dto.setJobsThisMonth(jobRepository.countByCreatedAtBetween(monthStart, now));
        dto.setApplicationsThisWeek(applicationRepository.countByAppliedAtBetween(weekStart, now));
        dto.setNewCompaniesThisMonth(companyRepository.countByCreatedAtBetween(monthStart, now));

        // ---------- RECENT ACTIVITY ----------
        dto.setRecentUsers(
                userRepository.findTop5ByOrderByCreatedAtDesc().stream()
                        .map(this::mapToRecentUser)
                        .collect(Collectors.toList())
        );
        dto.setRecentJobs(
                jobRepository.findTop5ByOrderByCreatedAtDesc().stream()
                        .map(this::mapToRecentJob)
                        .collect(Collectors.toList())
        );
        dto.setRecentApplications(
                applicationRepository.findTop5ByOrderByAppliedAtDesc().stream()
                        .map(this::mapToRecentApplication)
                        .collect(Collectors.toList())
        );

        // ---------- CHARTS ----------
        dto.setJobsPerIndustry(getJobsPerIndustry());
        dto.setApplicationsTrend(getApplicationsTrend());
        dto.setUsersByRole(getUsersByRole());

        // ---------- SYSTEM HEALTH ----------
        dto.setActiveRecruiters(
                userRepository.countByRole(Role.RECRUITER) +
                userRepository.countByRole(Role.RECRUITER_ADMIN)
        );
        dto.setJobsExpiringSoon(
                jobRepository.findJobsExpiringSoon(now, now.plusDays(7)).size()
        );
        dto.setJobsWithoutApplications(
                jobRepository.findJobsWithoutApplications().size()
        );
        dto.setPendingApprovals(0L);   // adjust if your app has an approval flow

        return dto;
    }

    // ==================== MAPPING HELPERS ====================

    private RecentUserDTO mapToRecentUser(User user) {
        RecentUserDTO dto = new RecentUserDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setJoined(user.getCreatedAt().toLocalDate().toString());
        if (user.getStatus() != null) {
            dto.setStatus(user.getStatus().name());
        }
        return dto;
    }

    private RecentJobDTO mapToRecentJob(Job job) {
        RecentJobDTO dto = new RecentJobDTO();
        dto.setTitle(job.getTitle());
        dto.setCompanyName(job.getCompany().getCompanyName());
        dto.setLocation(job.getLocation());
        dto.setIndustry(job.getIndustry());
        dto.setStatus(job.getStatus().name());
        dto.setPosted(job.getCreatedAt().toLocalDate().toString());
        return dto;
    }

    private RecentApplicationDTO mapToRecentApplication(JobApplication app) {
        RecentApplicationDTO dto = new RecentApplicationDTO();
        // Candidate name: if external application (fullName) else from jobSeeker
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
        dto.setApplicationSource(app.getApplicationSource().name());
        return dto;
    }

    private Map<String, Long> getJobsPerIndustry() {
        List<Object[]> results = jobRepository.countJobsPerIndustry();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    private List<DailyApplication> getApplicationsTrend() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> results = applicationRepository.countApplicationsPerDay(weekAgo);
        return results.stream()
                .map(row -> new DailyApplication(((Date) row[0]).toLocalDate(), (Long) row[1]))
                .collect(Collectors.toList());
    }

    private Map<String, Long> getUsersByRole() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            long count = userRepository.countByRole(role);
            if (count > 0) {
                map.put(role.name(), count);
            }
        }
        return map;
    }
}