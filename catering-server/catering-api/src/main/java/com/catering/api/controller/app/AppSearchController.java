package com.catering.api.controller.app;

import com.catering.common.result.Result;
import com.catering.service.search.SearchService;
import com.catering.service.search.dto.AiSearchRequest;
import com.catering.service.search.dto.AiSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "App - Search")
@RestController
@RequestMapping("/api/app/search")
@RequiredArgsConstructor
public class AppSearchController {

    private final SearchService searchService;

    @Operation(summary = "AI assisted search")
    @PostMapping("/ai")
    public Result<AiSearchResponse> aiSearch(@Valid @RequestBody AiSearchRequest request) {
        return Result.ok(searchService.aiSearch(request));
    }
}
