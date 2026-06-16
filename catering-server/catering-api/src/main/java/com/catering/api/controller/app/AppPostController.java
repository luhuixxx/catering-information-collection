package com.catering.api.controller.app;

import com.catering.api.security.model.AuthUserPrincipal;
import com.catering.common.result.Result;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.FranchisePostUpsertRequest;
import com.catering.service.post.dto.JobSeekPostUpsertRequest;
import com.catering.service.post.dto.MyPostVO;
import com.catering.service.post.dto.RentPostUpsertRequest;
import com.catering.service.post.dto.RecruitPostUpsertRequest;
import com.catering.service.post.dto.TransferPostUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@Tag(name = "App - Post")
@RestController
@RequestMapping("/api/app/posts")
@RequiredArgsConstructor
public class AppPostController {

    private final PostService postService;

    @Operation(summary = "保存招聘草稿")
    @PostMapping("/recruit/draft")
    public Result<Map<String, String>> saveRecruitDraft(@Valid @RequestBody RecruitPostUpsertRequest request,
                                                       Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(Map.of("postId", String.valueOf(postService.saveRecruitDraft(userId, request))));
    }

    @Operation(summary = "保存转让草稿")
    @PostMapping("/transfer/draft")
    public Result<Map<String, String>> saveTransferDraft(@Valid @RequestBody TransferPostUpsertRequest request,
                                                        Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(Map.of("postId", String.valueOf(postService.saveTransferDraft(userId, request))));
    }

    @Operation(summary = "保存出租草稿")
    @PostMapping("/rent/draft")
    public Result<Map<String, String>> saveRentDraft(@Valid @RequestBody RentPostUpsertRequest request,
                                                     Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(Map.of("postId", String.valueOf(postService.saveRentDraft(userId, request))));
    }

    @Operation(summary = "保存求职草稿")
    @PostMapping("/job-seek/draft")
    public Result<Map<String, String>> saveJobSeekDraft(@Valid @RequestBody JobSeekPostUpsertRequest request,
                                                        Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(Map.of("postId", String.valueOf(postService.saveJobSeekDraft(userId, request))));
    }

    @Operation(summary = "保存招商加盟草稿")
    @PostMapping("/franchise/draft")
    public Result<Map<String, String>> saveFranchiseDraft(@Valid @RequestBody FranchisePostUpsertRequest request,
                                                          Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(Map.of("postId", String.valueOf(postService.saveFranchiseDraft(userId, request))));
    }

    @Operation(summary = "提交审核")
    @PostMapping("/{postId}/submit")
    public Result<Void> submit(@PathVariable Long postId, Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        postService.submitForAudit(userId, postId);
        return Result.ok();
    }

    @Operation(summary = "获取可编辑帖子详情")
    @GetMapping("/{postId}/edit")
    public Result<Map<String, Object>> editDetail(@PathVariable String postId, Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(postService.getEditablePost(userId, Long.parseLong(postId)));
    }

    @Operation(summary = "更新招聘草稿")
    @PutMapping("/{postId}/recruit")
    public Result<Void> updateRecruit(@PathVariable String postId,
                                      @Valid @RequestBody RecruitPostUpsertRequest request,
                                      Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        postService.updateRecruitDraft(userId, Long.parseLong(postId), request);
        return Result.ok();
    }

    @Operation(summary = "更新转让草稿")
    @PutMapping("/{postId}/transfer")
    public Result<Void> updateTransfer(@PathVariable String postId,
                                       @Valid @RequestBody TransferPostUpsertRequest request,
                                       Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        postService.updateTransferDraft(userId, Long.parseLong(postId), request);
        return Result.ok();
    }

    @Operation(summary = "更新出租草稿")
    @PutMapping("/{postId}/rent")
    public Result<Void> updateRent(@PathVariable String postId,
                                   @Valid @RequestBody RentPostUpsertRequest request,
                                   Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        postService.updateRentDraft(userId, Long.parseLong(postId), request);
        return Result.ok();
    }

    @Operation(summary = "更新求职草稿")
    @PutMapping("/{postId}/job-seek")
    public Result<Void> updateJobSeek(@PathVariable String postId,
                                      @Valid @RequestBody JobSeekPostUpsertRequest request,
                                      Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        postService.updateJobSeekDraft(userId, Long.parseLong(postId), request);
        return Result.ok();
    }

    @Operation(summary = "更新招商加盟草稿")
    @PutMapping("/{postId}/franchise")
    public Result<Void> updateFranchise(@PathVariable String postId,
                                        @Valid @RequestBody FranchisePostUpsertRequest request,
                                        Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        postService.updateFranchiseDraft(userId, Long.parseLong(postId), request);
        return Result.ok();
    }

    @Operation(summary = "我的发布列表")
    @GetMapping("/mine")
    public Result<List<MyPostVO>> mine(@org.springframework.web.bind.annotation.RequestParam(required = false) String status,
                                       @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
                                       @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
                                       Authentication authentication) {
        Long userId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(postService.listMyPosts(userId, status, page, size));
    }
}
