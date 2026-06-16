package com.catering.api.controller.admin;

import com.catering.api.security.model.AuthUserPrincipal;
import com.catering.common.result.Result;
import com.catering.model.entity.PostAuditRecord;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.PendingPostVO;
import com.catering.service.post.dto.PostAuditActionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin - Post Audit Stage2")
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostAuditController {

    private final PostService postService;

    @Operation(summary = "待审核列表")
    @GetMapping("/pending")
    public Result<List<PendingPostVO>> pending(@RequestParam(required = false) String postType,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.listPendingPosts(postType, page, size));
    }

    @Operation(summary = "待审核详情")
    @GetMapping("/{postId}/detail")
    public Result<Map<String, Object>> detail(@PathVariable String postId) {
        return Result.ok(postService.getPendingPostDetail(Long.parseLong(postId)));
    }

    @Operation(summary = "审核操作：APPROVE / REJECT")
    @PostMapping("/{postId}/audit")
    public Result<PostAuditRecord> audit(@PathVariable String postId,
                                         @Valid @RequestBody PostAuditActionRequest request,
                                         Authentication authentication) {
        Long adminUserId = ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
        return Result.ok(postService.auditPendingPost(adminUserId, Long.parseLong(postId), request));
    }
}
