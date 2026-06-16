package com.catering.api.controller.common;

import com.catering.common.result.Result;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.PostListItemVO;
import com.catering.service.post.dto.PostPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Common - Search")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonSearchController {

    private final PostService postService;

    @Operation(summary = "公开信息搜索（ES 优先，失败回退数据库）")
    @GetMapping("/search")
    public Result<PostPageVO<PostListItemVO>> search(@RequestParam(required = false) String postType,
                                                   @RequestParam(required = false) Long cityId,
                                                   @RequestParam(required = false) Long districtId,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer minSalary,
                                                   @RequestParam(required = false) Integer maxSalary,
                                                   @RequestParam(required = false) String jobRole,
                                                   @RequestParam(required = false) String shopCategory,
                                                   @RequestParam(required = false) Boolean canCatering,
                                                   @RequestParam(required = false) Boolean canOpenFlame,
                                                   @RequestParam(required = false, defaultValue = "DEFAULT") String sort,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.listPublicPosts(postType, cityId, districtId, keyword,
                minSalary, maxSalary, jobRole, shopCategory, canCatering, canOpenFlame, sort, page, size));
    }
}
