package com.catering.api.controller.admin;

import com.catering.api.security.model.AuthUserPrincipal;
import com.catering.common.result.Result;
import com.catering.service.governance.GovernanceService;
import com.catering.service.governance.dto.AppUserVO;
import com.catering.service.governance.dto.PostReportHandleRequest;
import com.catering.service.governance.dto.PostReportVO;
import com.catering.service.governance.dto.UserBanRequest;
import com.catering.service.post.dto.PostPageVO;
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

@Tag(name = "Admin - Governance")
@RestController
@RequestMapping("/api/admin/governance")
@RequiredArgsConstructor
public class AdminGovernanceController {

    private final GovernanceService governanceService;

    @Operation(summary = "举报列表")
    @GetMapping("/reports")
    public Result<PostPageVO<PostReportVO>> reports(@RequestParam(required = false) String status,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return Result.ok(governanceService.listReports(status, page, size));
    }

    @Operation(summary = "处理举报")
    @PostMapping("/reports/{reportId}/handle")
    public Result<Void> handleReport(@PathVariable Long reportId,
                                     @Valid @RequestBody PostReportHandleRequest request,
                                     Authentication authentication) {
        governanceService.handleReport(adminUserId(authentication), reportId, request);
        return Result.ok();
    }

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<PostPageVO<AppUserVO>> users(@RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.ok(governanceService.listUsers(keyword, page, size));
    }

    @Operation(summary = "封禁用户")
    @PostMapping("/users/{userId}/ban")
    public Result<Void> banUser(@PathVariable Long userId,
                                @Valid @RequestBody UserBanRequest request,
                                Authentication authentication) {
        governanceService.banUser(adminUserId(authentication), userId, request);
        return Result.ok();
    }

    @Operation(summary = "解除封禁")
    @PostMapping("/users/{userId}/unban")
    public Result<Void> unbanUser(@PathVariable Long userId) {
        governanceService.unbanUser(userId);
        return Result.ok();
    }

    private Long adminUserId(Authentication authentication) {
        return ((AuthUserPrincipal) authentication.getPrincipal()).getUserId();
    }
}
