package com.catering.api.controller.app;

import com.catering.api.security.model.AuthUserPrincipal;
import com.catering.api.security.model.AuthUserType;
import com.catering.common.result.Result;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.PostDetailVO;
import com.catering.service.post.dto.PostListItemVO;
import com.catering.service.post.dto.PostPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "App - Post Browse")
@RestController
@RequestMapping("/api/app/post-browse")
@RequiredArgsConstructor
public class AppPostBrowseController {

    private final PostService postService;

    @Operation(summary = "公开信息列表")
    @GetMapping
    public Result<PostPageVO<PostListItemVO>> list(@RequestParam(required = false) String postType,
                                                   @RequestParam(required = false) Long cityId,
                                                   @RequestParam(required = false) Long districtId,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer minSalary,
                                                   @RequestParam(required = false) Integer maxSalary,
                                                   @RequestParam(required = false) String jobRole,
                                                   @RequestParam(required = false) String shopCategory,
                                                   @RequestParam(required = false) Boolean canCatering,
                                                   @RequestParam(required = false) Boolean canOpenFlame,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.listPublicPosts(postType, cityId, districtId, keyword,
                minSalary, maxSalary, jobRole, shopCategory, canCatering, canOpenFlame, page, size));
    }

    @Operation(summary = "公开信息详情，登录后返回完整电话")
    @GetMapping("/{postId}")
    public Result<PostDetailVO> detail(@PathVariable Long postId, Authentication authentication) {
        return Result.ok(postService.getPublicPostDetail(postId, isAppUser(authentication)));
    }

    private boolean isAppUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            return false;
        }
        return principal.getUserType() == AuthUserType.APP_USER;
    }
}
