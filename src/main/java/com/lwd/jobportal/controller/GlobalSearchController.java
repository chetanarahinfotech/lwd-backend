package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.search.GlobalSearchResponse;
import com.lwd.jobportal.dto.search.SearchSuggestionDTO;
import com.lwd.jobportal.service.GlobalSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Global search and suggestions APIs")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @Operation(
            summary = "Global search",
            description = "Search across jobs, companies, candidates, recruiters, or skills based on keyword and category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search request")
    })
    @Parameters({
            @Parameter(name = "keyword", description = "Search keyword (optional)"),
            @Parameter(name = "category", description = "Search category (jobs, companies, candidates, recruiters, skills)"),
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
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

    @Operation(
            summary = "Get search suggestions",
            description = "Fetch search suggestions based on keyword for autocomplete"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggestions fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid keyword")
    })
    @Parameter(name = "keyword", description = "Search keyword for suggestions", required = true)
    @GetMapping("/suggestions")
    public List<SearchSuggestionDTO> getSuggestions(
            @RequestParam String keyword
    ) {
        return globalSearchService.globalSearchSuggestions(keyword);
    }
}