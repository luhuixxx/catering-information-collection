package com.catering.service.governance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catering.common.exception.BusinessException;
import com.catering.dao.mapper.AppUserMapper;
import com.catering.dao.mapper.PostAuditRecordMapper;
import com.catering.dao.mapper.PostFavoriteMapper;
import com.catering.dao.mapper.PostMapper;
import com.catering.dao.mapper.PostReportMapper;
import com.catering.dao.mapper.SysRegionMapper;
import com.catering.model.entity.AppUser;
import com.catering.model.entity.Post;
import com.catering.model.entity.PostAuditRecord;
import com.catering.model.entity.PostFavorite;
import com.catering.model.entity.PostReport;
import com.catering.model.entity.SysRegion;
import com.catering.model.enums.PostStatus;
import com.catering.service.governance.dto.AppUserVO;
import com.catering.service.governance.dto.FavoritePostVO;
import com.catering.service.governance.dto.PostReportHandleRequest;
import com.catering.service.governance.dto.PostReportRequest;
import com.catering.service.governance.dto.PostReportVO;
import com.catering.service.governance.dto.UserBanRequest;
import com.catering.service.post.dto.PostListItemVO;
import com.catering.service.post.dto.PostPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GovernanceService {

    private final PostMapper postMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostReportMapper postReportMapper;
    private final AppUserMapper appUserMapper;
    private final SysRegionMapper sysRegionMapper;
    private final PostAuditRecordMapper postAuditRecordMapper;

    @Transactional
    public void favorite(Long userId, Long postId) {
        ensureActiveUser(userId);
        Post post = visiblePost(postId);
        PostFavorite exists = postFavoriteMapper.selectOne(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, post.getId())
                .last("LIMIT 1"));
        if (exists != null) {
            return;
        }
        PostFavorite favorite = new PostFavorite();
        favorite.setUserId(userId);
        favorite.setPostId(post.getId());
        favorite.setCreatedAt(LocalDateTime.now());
        postFavoriteMapper.insert(favorite);
    }

    @Transactional
    public void unfavorite(Long userId, Long postId) {
        postFavoriteMapper.delete(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId));
    }

    public boolean isFavorited(Long userId, Long postId) {
        if (userId == null) {
            return false;
        }
        return postFavoriteMapper.selectCount(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId)) > 0;
    }

    public List<FavoritePostVO> listFavorites(Long userId, int page, int size) {
        Page<PostFavorite> pageReq = new Page<>(safePage(page), safeSize(size));
        Page<PostFavorite> result = postFavoriteMapper.selectPage(pageReq, new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .orderByDesc(PostFavorite::getCreatedAt));
        return result.getRecords().stream()
                .map(favorite -> {
                    Post post = postMapper.selectById(favorite.getPostId());
                    return FavoritePostVO.builder()
                            .favoriteId(String.valueOf(favorite.getId()))
                            .favoritedAt(favorite.getCreatedAt())
                            .post(post == null ? null : toListItem(post))
                            .build();
                })
                .filter(item -> item.getPost() != null)
                .toList();
    }

    @Transactional
    public void report(Long userId, Long postId, PostReportRequest request) {
        ensureActiveUser(userId);
        Post post = visiblePost(postId);
        PostReport exists = postReportMapper.selectOne(new LambdaQueryWrapper<PostReport>()
                .eq(PostReport::getReporterUserId, userId)
                .eq(PostReport::getPostId, post.getId())
                .last("LIMIT 1"));
        if (exists != null) {
            throw new BusinessException(400, "你已举报过该信息，运营会尽快处理");
        }
        PostReport report = new PostReport();
        report.setPostId(post.getId());
        report.setReporterUserId(userId);
        report.setReason(normalizeReportReason(request.getReason()));
        report.setDescription(nvl(request.getDescription()));
        report.setEvidenceImage(nvl(request.getEvidenceImage()));
        report.setStatus("PENDING");
        report.setHandledAction("");
        report.setHandledNote("");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        postReportMapper.insert(report);
    }

    public PostPageVO<PostReportVO> listReports(String status, int page, int size) {
        Page<PostReport> pageReq = new Page<>(safePage(page), safeSize(size));
        LambdaQueryWrapper<PostReport> wrapper = new LambdaQueryWrapper<PostReport>()
                .orderByDesc(PostReport::getCreatedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(PostReport::getStatus, status);
        }
        Page<PostReport> result = postReportMapper.selectPage(pageReq, wrapper);
        return PostPageVO.<PostReportVO>builder()
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .records(result.getRecords().stream().map(this::toReportVO).toList())
                .build();
    }

    @Transactional
    public void handleReport(Long adminUserId, Long reportId, PostReportHandleRequest request) {
        PostReport report = postReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(404, "举报不存在");
        }
        if ("DONE".equals(report.getStatus())) {
            throw new BusinessException(400, "举报已处理");
        }
        String action = normalizeHandleAction(request.getAction());
        Post post = postMapper.selectById(report.getPostId());
        if ("OFFLINE".equals(action) || "BAN".equals(action)) {
            if (post != null && post.getStatus() != PostStatus.OFFLINE) {
                post.setStatus(PostStatus.OFFLINE);
                postMapper.updateById(post);
                writeAudit(post.getId(), adminUserId, "OFFLINE", action, nvl(request.getNote()));
            }
        }
        if ("BAN".equals(action)) {
            if (post == null || post.getPublisherUserId() == null) {
                throw new BusinessException(400, "无法定位发布者");
            }
            banUser(adminUserId, post.getPublisherUserId(), request.getBanDays(), nvl(request.getNote()), true);
        }
        report.setStatus("DONE");
        report.setHandledByAdminId(adminUserId);
        report.setHandledAction(action);
        report.setHandledNote(nvl(request.getNote()));
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        postReportMapper.updateById(report);
    }

    public PostPageVO<AppUserVO> listUsers(String keyword, int page, int size) {
        Page<AppUser> pageReq = new Page<>(safePage(page), safeSize(size));
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<AppUser>()
                .orderByDesc(AppUser::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(AppUser::getPhone, keyword).or().like(AppUser::getNickname, keyword));
        }
        Page<AppUser> result = appUserMapper.selectPage(pageReq, wrapper);
        return PostPageVO.<AppUserVO>builder()
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .records(result.getRecords().stream().map(this::toUserVO).toList())
                .build();
    }

    @Transactional
    public void banUser(Long adminUserId, Long userId, UserBanRequest request) {
        banUser(adminUserId, userId, request == null ? null : request.getBanDays(),
                request == null ? "" : nvl(request.getReason()),
                request != null && request.isOfflinePosts());
    }

    @Transactional
    public void unbanUser(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setBannedUntil(null);
        user.setBanReason("");
        appUserMapper.updateById(user);
    }

    private void banUser(Long adminUserId, Long userId, Integer banDays, String reason, boolean offlinePosts) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        int days = banDays == null ? 7 : banDays;
        if (days < 1 || days > 3650) {
            throw new BusinessException(400, "封禁天数需在 1-3650 天之间");
        }
        user.setBannedUntil(LocalDateTime.now().plusDays(days));
        user.setBanReason(reason.isBlank() ? "违规处理" : reason);
        user.setViolationCount((user.getViolationCount() == null ? 0 : user.getViolationCount()) + 1);
        appUserMapper.updateById(user);
        if (offlinePosts) {
            List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .eq(Post::getPublisherUserId, userId)
                    .eq(Post::getStatus, PostStatus.APPROVED));
            for (Post post : posts) {
                post.setStatus(PostStatus.OFFLINE);
                postMapper.updateById(post);
                writeAudit(post.getId(), adminUserId, "OFFLINE", "BAN_USER", reason);
            }
        }
    }

    private Post visiblePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != PostStatus.APPROVED || post.getExpireAt() == null || !post.getExpireAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(404, "信息不存在或已失效");
        }
        return post;
    }

    private void ensureActiveUser(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(403, "账号已被封禁，暂不能操作");
        }
    }

    private PostReportVO toReportVO(PostReport report) {
        Post post = postMapper.selectById(report.getPostId());
        AppUser reporter = appUserMapper.selectById(report.getReporterUserId());
        return PostReportVO.builder()
                .id(String.valueOf(report.getId()))
                .postId(String.valueOf(report.getPostId()))
                .postNo(post == null ? "" : post.getPostNo())
                .postTitle(post == null ? "信息已删除" : post.getTitle())
                .postStatus(post == null || post.getStatus() == null ? "" : post.getStatus().name())
                .publisherUserId(post == null || post.getPublisherUserId() == null ? "" : String.valueOf(post.getPublisherUserId()))
                .reporterUserId(String.valueOf(report.getReporterUserId()))
                .reporterPhone(reporter == null ? "" : reporter.getPhone())
                .reason(report.getReason())
                .description(report.getDescription())
                .evidenceImage(report.getEvidenceImage())
                .status(report.getStatus())
                .handledAction(report.getHandledAction())
                .handledNote(report.getHandledNote())
                .handledAt(report.getHandledAt())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private AppUserVO toUserVO(AppUser user) {
        LocalDateTime now = LocalDateTime.now();
        return AppUserVO.builder()
                .id(String.valueOf(user.getId()))
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .violationCount(user.getViolationCount())
                .bannedUntil(user.getBannedUntil())
                .banReason(user.getBanReason())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .postCount(postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getPublisherUserId, user.getId())))
                .reportCount(postReportMapper.selectCount(new LambdaQueryWrapper<PostReport>().eq(PostReport::getReporterUserId, user.getId())))
                .banned(user.getBannedUntil() != null && user.getBannedUntil().isAfter(now))
                .build();
    }

    private PostListItemVO toListItem(Post post) {
        return PostListItemVO.builder()
                .id(String.valueOf(post.getId()))
                .postNo(post.getPostNo())
                .postType(post.getPostType() == null ? "" : post.getPostType().name())
                .status(post.getStatus() == null ? "" : post.getStatus().name())
                .title(post.getTitle())
                .cityId(post.getCityId())
                .cityName(regionName(post.getCityId()))
                .districtId(post.getDistrictId())
                .districtName(regionName(post.getDistrictId()))
                .summary(post.getDescription() == null || post.getDescription().isBlank() ? post.getTitle() : post.getDescription())
                .coverImage(post.getCoverImage())
                .isTop(post.getIsTop())
                .topUntil(post.getTopUntil())
                .createdAt(post.getCreatedAt())
                .expireAt(post.getExpireAt())
                .build();
    }

    private String regionName(Long regionId) {
        if (regionId == null) {
            return "";
        }
        SysRegion region = sysRegionMapper.selectById(regionId);
        return region == null ? "" : region.getName();
    }

    private void writeAudit(Long postId, Long adminUserId, String action, String code, String note) {
        PostAuditRecord record = new PostAuditRecord();
        record.setPostId(postId);
        record.setAction(action);
        record.setReasonCode(code);
        record.setReasonText(note);
        record.setOperatorAdminId(adminUserId);
        postAuditRecordMapper.insert(record);
    }

    private String normalizeReportReason(String reason) {
        if (reason == null) {
            throw new BusinessException(400, "请选择举报原因");
        }
        String value = reason.trim().toUpperCase();
        if (!List.of("FAKE", "SCAM", "SPAM", "ABUSE", "OTHER").contains(value)) {
            throw new BusinessException(400, "举报原因不正确");
        }
        return value;
    }

    private String normalizeHandleAction(String action) {
        if (action == null) {
            throw new BusinessException(400, "请选择处理方式");
        }
        String value = action.trim().toUpperCase();
        if (!List.of("IGNORE", "OFFLINE", "BAN").contains(value)) {
            throw new BusinessException(400, "处理方式不正确");
        }
        return value;
    }

    private int safePage(int page) {
        return Math.max(page, 1);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 50);
    }

    private String nvl(String value) {
        return value == null ? "" : value.trim();
    }
}
