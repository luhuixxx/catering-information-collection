package com.catering.api.controller.app;

import com.catering.api.security.model.AuthUserPrincipal;
import com.catering.common.result.Result;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.RecruitPostUpsertRequest;
import com.catering.service.post.dto.TransferPostUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "App - Post Stage2")
@RestController
@RequestMapping("/api/app/posts")
@RequiredArgsConstructor
public class AppPostController {

    private final PostService postService;

    @Operation(summary = "保存招聘草稿")
    @PostMapping("/recruit/draft")
    public Result<Map<String, Long>> saveRecruitDraft(@Valid @RequestBody RecruitPostUpsertRequest request,
                                                       Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(Map.of("postId", postService.saveRecruitDraft(userId, request)));
    }

    @Operation(summary = "保存转让草稿")
    @PostMapping("/transfer/draft")
    public Result<Map<String, Long>> saveTransferDraft(@Valid @RequestBody TransferPostUpsertRequest request,
                                                        Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(Map.of("postId", postService.saveTransferDraft(userId, request)));
    }

    @Operation(summary = "提交审核")
    @PostMapping("/{postId}/submit")
    public Result<Void> submit(@PathVariable Long postId, Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        postService.submitForAudit(userId, postId);
        return Result.ok();
    }
}
