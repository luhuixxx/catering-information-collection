package com.catering.api.controller.app;

import com.catering.api.security.model.AuthUserPrincipal;
import com.catering.common.result.Result;
import com.catering.service.governance.GovernanceService;
import com.catering.service.governance.dto.FavoritePostVO;
import com.catering.service.governance.dto.PostReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "App - Governance")
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppGovernanceController {

    private final GovernanceService governanceService;

    @Operation(summary = "收藏信息")
    @PostMapping("/posts/{postId}/favorite")
    public Result<Void> favorite(@PathVariable Long postId, Authentication authentication) {
        governanceService.favorite(userId(authentication), postId);
        return Result.ok();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/posts/{postId}/favorite")
    public Result<Void> unfavorite(@PathVariable Long postId, Authentication authentication) {
        governanceService.unfavorite(userId(authentication), postId);
        return Result.ok();
    }

    @Operation(summary = "收藏状态")
    @GetMapping("/posts/{postId}/favorite")
    public Result<Map<String, Boolean>> favoriteStatus(@PathVariable Long postId, Authentication authentication) {
        return Result.ok(Map.of("favorited", governanceService.isFavorited(userId(authentication), postId)));
    }

    @Operation(summary = "我的收藏")
    @GetMapping("/favorites")
    public Result<List<FavoritePostVO>> favorites(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  Authentication authentication) {
        return Result.ok(governanceService.listFavorites(userId(authentication), page, size));
    }

    @Operation(summary = "举报信息")
    @PostMapping("/posts/{postId}/report")
    public Result<Void> report(@PathVariable Long postId,
                               @Valid @RequestBody PostReportRequest request,
                               Authentication authentication) {
        governanceService.report(userId(authentication), postId, request);
        return Result.ok();
    }

    private Long userId(Authentication authentication) {
        return ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
    }
}
