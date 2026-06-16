package com.catering.api.controller.admin;

import com.catering.common.result.Result;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.PostDetailVO;
import com.catering.service.post.dto.PostListItemVO;
import com.catering.service.post.dto.PostPageVO;
import com.catering.service.post.dto.PostTopRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Post Manage")
@RestController
@RequestMapping("/api/admin/post-manage")
@RequiredArgsConstructor
public class AdminPostManageController {

    private final PostService postService;

    @Operation(summary = "运营视角信息检索")
    @GetMapping
    public Result<PostPageVO<PostListItemVO>> list(@RequestParam(required = false) String postType,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Long cityId,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String phone,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.listAdminPosts(postType, status, cityId, keyword, phone, page, size));
    }

    @Operation(summary = "运营视角信息详情")
    @GetMapping("/{postId}")
    public Result<PostDetailVO> detail(@PathVariable Long postId) {
        return Result.ok(postService.getAdminPostDetail(postId));
    }

    @Operation(summary = "免费置顶")
    @PostMapping("/{postId}/top")
    public Result<Void> top(@PathVariable Long postId, @RequestBody(required = false) PostTopRequest request) {
        postService.setTop(postId, request);
        return Result.ok();
    }

    @Operation(summary = "取消置顶")
    @PostMapping("/{postId}/top/cancel")
    public Result<Void> cancelTop(@PathVariable Long postId) {
        postService.cancelTop(postId);
        return Result.ok();
    }
}
