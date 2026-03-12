//package com.lwd.jobportal.service.elasticsearch;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import com.lwd.jobportal.dto.search.CompanySearchDTO;
//import com.lwd.jobportal.dto.search.GlobalSearchResponse;
//import com.lwd.jobportal.dto.search.JobSearchDTO;
//import com.lwd.jobportal.dto.search.SearchSuggestionDTO;
//import com.lwd.jobportal.dto.search.SkillDTO;
//import com.lwd.jobportal.dto.search.UserSearchDTO;
//import com.lwd.jobportal.repository.elasticsearch.CompanySearchRepository;
//import com.lwd.jobportal.repository.elasticsearch.JobSearchRepository;
//import com.lwd.jobportal.repository.elasticsearch.SkillSearchRepository;
//import com.lwd.jobportal.repository.elasticsearch.UserSearchRepository;
//import com.lwd.jobportal.search.document.CompanyDocument;
//import com.lwd.jobportal.search.document.JobDocument;
//import com.lwd.jobportal.search.document.SkillDocument;
//import com.lwd.jobportal.search.document.UserDocument;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class GlobalSearchService {
//
//    private final JobSearchRepository jobSearchRepository;
//    private final CompanySearchRepository companySearchRepository;
//    private final UserSearchRepository userSearchRepository;
//    private final SkillSearchRepository skillSearchRepository;
//
//    public GlobalSearchResponse globalSearch(String keyword, String category, Pageable pageable) {
//
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return new GlobalSearchResponse();
//        }
//
//        Page<JobDocument> jobs =
//                jobSearchRepository.findByTitleContainingIgnoreCase(keyword, pageable);
//
//        Page<CompanyDocument> companies =
//                companySearchRepository.findByCompanyNameContainingIgnoreCase(keyword, pageable);
//
//        Page<UserDocument> users =
//                userSearchRepository.findByNameContainingIgnoreCase(keyword, pageable);
//
//        Page<SkillDocument> skills =
//                skillSearchRepository.findByNameContainingIgnoreCase(keyword, pageable);
//
//        List<UserSearchDTO> candidates = users.stream()
//                .filter(u -> "JOB_SEEKER".equalsIgnoreCase(u.getRole()))
//                .map(this::mapUser)
//                .toList();
//
//        List<UserSearchDTO> recruiters = users.stream()
//                .filter(u -> "RECRUITER".equalsIgnoreCase(u.getRole()))
//                .map(this::mapUser)
//                .toList();
//
//        return GlobalSearchResponse.builder()
//                .jobs(jobs.stream().map(this::mapJob).toList())
//                .companies(companies.stream().map(this::mapCompany).toList())
//                .candidates(candidates)
//                .recruiters(recruiters)
//                .skills(skills.stream().map(this::mapSkill).toList())
//                .totalPages(jobs.getTotalPages())
//                .totalElements(jobs.getTotalElements())
//                .build();
//    }
//
//    public List<SearchSuggestionDTO> globalSearchSuggestions(String keyword) {
//
//        Pageable limit = PageRequest.of(0, 3);
//
//        List<SearchSuggestionDTO> suggestions = new ArrayList<>();
//
//        jobSearchRepository.findByTitleContainingIgnoreCase(keyword, limit)
//                .forEach(job -> suggestions.add(
//                        new SearchSuggestionDTO(job.getId(), job.getTitle(), "JOB")
//                ));
//
//        companySearchRepository.findByCompanyNameContainingIgnoreCase(keyword, limit)
//                .forEach(company -> suggestions.add(
//                        new SearchSuggestionDTO(company.getId(), company.getCompanyName(), "COMPANY")
//                ));
//
//        skillSearchRepository.findByNameContainingIgnoreCase(keyword, limit)
//                .forEach(skill -> suggestions.add(
//                        new SearchSuggestionDTO(skill.getId(), skill.getName(), "SKILL")
//                ));
//
//        userSearchRepository.findByNameContainingIgnoreCase(keyword, limit)
//                .forEach(user -> suggestions.add(
//                        new SearchSuggestionDTO(user.getId(), user.getName(), user.getRole())
//                ));
//
//        return suggestions.stream().limit(10).toList();
//    }
//
//    private JobSearchDTO mapJob(JobDocument job) {
//
//        return JobSearchDTO.builder()
//                .id(job.getId())
//                .title(job.getTitle())
//                .location(job.getLocation())
//                .industry(job.getIndustry())
//                .companyName(job.getCompanyName())
//                .jobType(job.getJobType())
//                .build();
//    }
//
//    private CompanySearchDTO mapCompany(CompanyDocument company) {
//
//        return CompanySearchDTO.builder()
//                .id(company.getId())
//                .companyName(company.getCompanyName())
//                .location(company.getLocation())
//                .industry(company.getIndustry())
//                .build();
//    }
//
//    private UserSearchDTO mapUser(UserDocument user) {
//
//        return UserSearchDTO.builder()
//                .id(user.getId())
//                .name(user.getName())
//                .email(user.getEmail())
//                .phone(user.getPhone())
//                .companyName(user.getCompanyName())
//                .build();
//    }
//
//    private SkillDTO mapSkill(SkillDocument skill) {
//
//        return new SkillDTO(skill.getId(), skill.getName());
//    }
//}


