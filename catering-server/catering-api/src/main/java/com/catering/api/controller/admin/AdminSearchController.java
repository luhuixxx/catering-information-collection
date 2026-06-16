package com.catering.api.controller.admin;

import com.catering.common.result.Result;
import com.catering.service.search.PostEsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Admin - Search")
@RestController
@RequestMapping("/api/admin/search")
@RequiredArgsConstructor
public class AdminSearchController {

    private final PostEsService postEsService;

    @Operation(summary = "Elasticsearch health check")
    @GetMapping("/es/health")
    public Result<Map<String, Object>> esHealth() {
        return Result.ok(postEsService.healthCheck());
    }

    @Operation(summary = "Rebuild Elasticsearch post index")
    @PostMapping("/es/rebuild")
    public Result<Map<String, Object>> esRebuild() {
        int synced = postEsService.rebuildIndex();
        Map<String, Object> payload = new LinkedHashMap<>(postEsService.healthCheck());
        payload.put("synced", synced);
        return Result.ok(payload);
    }
}
