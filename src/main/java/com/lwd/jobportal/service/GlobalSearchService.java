package com.lwd.jobportal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.search.CompanySearchDTO;
import com.lwd.jobportal.dto.search.GlobalSearchResponse;
import com.lwd.jobportal.dto.search.JobSearchDTO;
import com.lwd.jobportal.dto.search.SearchSuggestionDTO;
import com.lwd.jobportal.dto.search.SkillDTO;
import com.lwd.jobportal.dto.search.UserSearchDTO;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.Skill;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.SkillRepository;
import com.lwd.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public GlobalSearchResponse globalSearch(String keyword, String category, Pageable pageable) {

        if (keyword == null || keyword.trim().isEmpty()) {

            return switch (category.toLowerCase()) {

                case "jobs" -> {
                    Page<Job> page = jobRepository.findAll(pageable);
                    yield GlobalSearchResponse.builder()
                            .jobs(page.stream().map(this::mapJob).toList())
                            .totalPages(page.getTotalPages())
                            .totalElements(page.getTotalElements())
                            .build();
                }

                case "companies" -> {
                    Page<Company> page = companyRepository.findAll(pageable);
                    yield GlobalSearchResponse.builder()
                            .companies(page.stream().map(this::mapCompany).toList())
                            .totalPages(page.getTotalPages())
                            .totalElements(page.getTotalElements())
                            .build();
                }

                case "candidates" -> {
                    Page<User> page = userRepository.findJobSeekers(pageable);
                    yield GlobalSearchResponse.builder()
                            .candidates(page.stream().map(this::mapUser).toList())
                            .totalPages(page.getTotalPages())
                            .totalElements(page.getTotalElements())
                            .build();
                }

                case "recruiters" -> {
                    Page<User> page = userRepository.findRecruiters(pageable);
                    yield GlobalSearchResponse.builder()
                            .recruiters(page.stream().map(this::mapUser).toList())
                            .totalPages(page.getTotalPages())
                            .totalElements(page.getTotalElements())
                            .build();
                }

                case "skills" -> {
                    Page<Skill> page = skillRepository.findAll(pageable);
                    yield GlobalSearchResponse.builder()
                            .skills(page.stream().map(this::mapSkill).toList())
                            .totalPages(page.getTotalPages())
                            .totalElements(page.getTotalElements())
                            .build();
                }

                default -> new GlobalSearchResponse();
            };
        }

        // SEARCH MODE

        Page<Job> jobs = jobRepository.searchJobs(keyword, pageable);
        Page<Company> companies = companyRepository.searchCompanies(keyword, pageable);
        Page<User> candidates = userRepository.searchJobSeekers(keyword, pageable);
        Page<User> recruiters = userRepository.searchRecruiters(keyword, pageable);
        Page<Skill> skills = skillRepository.searchSkills(keyword, pageable);

        return GlobalSearchResponse.builder()
                .jobs(jobs.stream().map(this::mapJob).toList())
                .companies(companies.stream().map(this::mapCompany).toList())
                .candidates(candidates.stream().map(this::mapUser).toList())
                .recruiters(recruiters.stream().map(this::mapUser).toList())
                .skills(skills.stream().map(this::mapSkill).toList())
                .totalPages(jobs.getTotalPages()) // same pageable
                .totalElements(jobs.getTotalElements())
                .build();
    }
    
    
    public GlobalSearchResponse searchJobs(String keyword, Pageable pageable) {

        Page<Job> page = keyword == null || keyword.trim().isEmpty()
                ? jobRepository.findAll(pageable)
                : jobRepository.searchJobs(keyword, pageable);

        return GlobalSearchResponse.builder()
                .jobs(page.stream().map(this::mapJob).toList())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    
    public GlobalSearchResponse searchCandidates(String keyword, Pageable pageable) {

        Page<User> page = keyword == null || keyword.trim().isEmpty()
                ? userRepository.findJobSeekers(pageable)
                : userRepository.searchJobSeekers(keyword, pageable);

        return GlobalSearchResponse.builder()
                .candidates(page.stream().map(this::mapUser).toList())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    
    public GlobalSearchResponse searchRecruiters(String keyword, Pageable pageable) {

        Page<User> page = keyword == null || keyword.trim().isEmpty()
                ? userRepository.findRecruiters(pageable)
                : userRepository.searchRecruiters(keyword, pageable);

        return GlobalSearchResponse.builder()
                .recruiters(page.stream().map(this::mapUser).toList())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    
    public GlobalSearchResponse searchSkills(String keyword, Pageable pageable) {

        Page<Skill> page = keyword == null || keyword.trim().isEmpty()
                ? skillRepository.findAll(pageable)
                : skillRepository.searchSkills(keyword, pageable);

        return GlobalSearchResponse.builder()
                .skills(page.stream().map(this::mapSkill).toList())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }


    
    
    public List<SearchSuggestionDTO> globalSearchSuggestions(String keyword) {

        Pageable limit = PageRequest.of(0, 3);

        List<SearchSuggestionDTO> suggestions = new ArrayList<>();

        // Job suggestions
        jobRepository.searchJobSuggestions(keyword, limit)
                .forEach(job -> suggestions.add(
                        new SearchSuggestionDTO(
                                job.getId(),
                                job.getTitle(),
                                "JOB"
                        )
                ));

        // Company suggestions
        companyRepository.searchCompanySuggestions(keyword, limit)
                .forEach(company -> suggestions.add(
                        new SearchSuggestionDTO(
                                company.getId(),
                                company.getCompanyName(),
                                "COMPANY"
                        )
                ));

        // Skill suggestions
        skillRepository.searchSkillSuggestions(keyword, limit)
                .forEach(skill -> suggestions.add(
                        new SearchSuggestionDTO(
                                skill.getId(),
                                skill.getName(),
                                "SKILL"
                        )
                ));

        // Candidate suggestions
        userRepository.searchJobSeekerSuggestions(keyword, limit)
                .forEach(user -> suggestions.add(
                        new SearchSuggestionDTO(
                                user.getId(),
                                user.getName(),
                                "CANDIDATE"
                        )
                ));

        // Recruiter suggestions
        userRepository.searchRecruiterSuggestions(keyword, limit)
                .forEach(user -> suggestions.add(
                        new SearchSuggestionDTO(
                                user.getId(),
                                user.getName(),
                                "RECRUITER"
                        )
                ));

        return suggestions.stream()
                .limit(10)
                .toList();
    }





    
    
    private JobSearchDTO mapJob(Job job) {
        return JobSearchDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .location(job.getLocation())
                .industry(job.getIndustry())
                .companyName(job.getCompany().getCompanyName())
                .jobType(job.getJobType().name())
                .build();
    }

    private CompanySearchDTO mapCompany(Company company) {
        return CompanySearchDTO.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .location(company.getLocation())
                .industry(company.getIndustry())
                .build();
    }

    private UserSearchDTO mapUser(User user) {
        return UserSearchDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .companyName(
                        user.getCompany() != null
                                ? user.getCompany().getCompanyName()
                                : null
                )
                .build();
    }

    private SkillDTO mapSkill(Skill skill) {
        return new SkillDTO(skill.getId(), skill.getName());
    }
}

