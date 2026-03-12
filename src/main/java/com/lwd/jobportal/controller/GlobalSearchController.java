package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lwd.jobportal.dto.search.GlobalSearchResponse;
import com.lwd.jobportal.dto.search.SearchSuggestionDTO;
import com.lwd.jobportal.service.GlobalSearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    public GlobalSearchResponse search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "jobs") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return globalSearchService.globalSearch(keyword, category, pageable);
    }
    
    
    @GetMapping("/suggestions")
    public List<SearchSuggestionDTO> getSuggestions(
            @RequestParam String keyword
    ) {
        return globalSearchService.globalSearchSuggestions(keyword);
    }

    

}
