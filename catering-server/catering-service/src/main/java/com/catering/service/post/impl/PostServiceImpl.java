package com.catering.service.post.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.common.exception.BusinessException;
import com.catering.dao.mapper.AppUserMapper;
import com.catering.dao.mapper.PostAuditRecordMapper;
import com.catering.dao.mapper.PostFranchiseMapper;
import com.catering.dao.mapper.PostImageMapper;
import com.catering.dao.mapper.PostJobSeekMapper;
import com.catering.dao.mapper.PostMapper;
import com.catering.dao.mapper.PostRentMapper;
import com.catering.dao.mapper.PostRecruitMapper;
import com.catering.dao.mapper.PostTransferMapper;
import com.catering.dao.mapper.SysRegionMapper;
import com.catering.model.entity.AppUser;
import com.catering.model.entity.PostFranchise;
import com.catering.model.entity.PostJobSeek;
import com.catering.model.entity.PostRent;
import com.catering.model.entity.PostAuditRecord;
import com.catering.model.entity.PostImage;
import com.catering.model.entity.Post;
import com.catering.model.entity.PostRecruit;
import com.catering.model.entity.PostTransfer;
import com.catering.model.entity.SysRegion;
import com.catering.model.enums.PostStatus;
import com.catering.model.enums.PostType;
import com.catering.model.enums.PublisherIdentity;
import com.catering.model.enums.SalaryType;
import com.catering.service.config.SysConfigService;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.MyPostVO;
import com.catering.service.post.dto.PendingPostVO;
import com.catering.service.post.dto.FranchisePostUpsertRequest;
import com.catering.service.post.dto.JobSeekPostUpsertRequest;
import com.catering.service.post.dto.PostAuditActionRequest;
import com.catering.service.post.dto.PostDetailVO;
import com.catering.service.post.dto.PostListItemVO;
import com.catering.service.post.dto.PostPageVO;
import com.catering.service.post.dto.PostTopRequest;
import com.catering.service.post.dto.RentPostUpsertRequest;
import com.catering.service.post.dto.RecruitPostUpsertRequest;
import com.catering.service.post.dto.TransferPostUpsertRequest;
import com.catering.service.search.PostEsService;
import com.catering.service.search.dto.PostSearchQuery;
import com.catering.service.search.dto.PostSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 30;

    private final PostMapper postMapper;
    private final PostRecruitMapper postRecruitMapper;
    private final PostTransferMapper postTransferMapper;
    private final PostRentMapper postRentMapper;
    private final PostJobSeekMapper postJobSeekMapper;
    private final PostFranchiseMapper postFranchiseMapper;
    private final PostImageMapper postImageMapper;
    private final PostAuditRecordMapper postAuditRecordMapper;
    private final SysRegionMapper sysRegionMapper;
    private final AppUserMapper appUserMapper;
    private final SysConfigService sysConfigService;
    private final PostEsService postEsService;

    @Override
    @Transactional
    public Long saveRecruitDraft(Long userId, RecruitPostUpsertRequest request) {
        ensureActiveUser(userId);
        validateRecruitRequest(request);
        Post post = buildBasePost(userId, PostType.RECRUIT, request.getTitle(), request.getCityId(), request.getDistrictId(),
                request.getAddress(), request.getContactName(), request.getContactPhone(), request.getContactWechat(),
                request.getDescription(), request.getImages(), request.getExpireDays());
        post.setStatus(PostStatus.DRAFT);
        postMapper.insert(post);

        PostRecruit recruit = new PostRecruit();
        recruit.setPostId(post.getId());
        recruit.setJobRole(request.getJobRole());
        recruit.setJobRoleOther(nvl(request.getJobRoleOther()));
        recruit.setShopCategory(nvl(request.getShopCategory()));
        recruit.setSalaryType(parseSalaryType(request.getSalaryType()));
        recruit.setSalaryMin(request.getSalaryMin());
        recruit.setSalaryMax(request.getSalaryMax());
        recruit.setProvideBoard(defaultInt(request.getProvideBoard(), 0));
        recruit.setHeadcount(defaultInt(request.getHeadcount(), 1));
        recruit.setExpRequirement(nvl(request.getExpRequirement()));
        recruit.setAgeRequirement(nvl(request.getAgeRequirement()));
        recruit.setCuisines(nvl(request.getCuisines()));
        recruit.setWorkTimeDesc(nvl(request.getWorkTimeDesc()));
        postRecruitMapper.insert(recruit);
        saveImages(post.getId(), request.getImages());
        return post.getId();
    }

    @Override
    @Transactional
    public Long saveTransferDraft(Long userId, TransferPostUpsertRequest request) {
        ensureActiveUser(userId);
        validateTransferRequest(request);
        Post post = buildBasePost(userId, PostType.TRANSFER, request.getTitle(), request.getCityId(), request.getDistrictId(),
                request.getAddress(), request.getContactName(), request.getContactPhone(), request.getContactWechat(),
                request.getDescription(), request.getImages(), request.getExpireDays());
        post.setStatus(PostStatus.DRAFT);
        postMapper.insert(post);

        PostTransfer transfer = new PostTransfer();
        transfer.setPostId(post.getId());
        transfer.setShopCategory(request.getShopCategory());
        transfer.setAreaSqm(request.getAreaSqm());
        transfer.setRentMonthly(request.getRentMonthly());
        transfer.setRentNegotiable(defaultInt(request.getRentNegotiable(), 0));
        transfer.setTransferFee(request.getTransferFee());
        transfer.setFeeNegotiable(defaultInt(request.getFeeNegotiable(), 0));
        transfer.setIncludeEquipment(defaultInt(request.getIncludeEquipment(), 0));
        transfer.setOperating(defaultInt(request.getOperating(), 0));
        transfer.setRevenueDesc(nvl(request.getRevenueDesc()));
        transfer.setReason(nvl(request.getReason()));
        postTransferMapper.insert(transfer);
        saveImages(post.getId(), request.getImages());
        return post.getId();
    }

    @Override
    @Transactional
    public Long saveRentDraft(Long userId, RentPostUpsertRequest request) {
        ensureActiveUser(userId);
        validateRentRequest(request);
        Post post = buildBasePost(userId, PostType.RENT, request.getTitle(), request.getCityId(), request.getDistrictId(),
                request.getAddress(), request.getContactName(), request.getContactPhone(), request.getContactWechat(),
                request.getDescription(), request.getImages(), request.getExpireDays());
        post.setStatus(PostStatus.DRAFT);
        postMapper.insert(post);

        PostRent rent = new PostRent();
        applyRent(rent, post.getId(), request);
        postRentMapper.insert(rent);
        saveImages(post.getId(), request.getImages());
        return post.getId();
    }

    @Override
    @Transactional
    public Long saveJobSeekDraft(Long userId, JobSeekPostUpsertRequest request) {
        ensureActiveUser(userId);
        validateJobSeekRequest(request);
        Post post = buildBasePost(userId, PostType.JOB_SEEK, request.getTitle(), request.getCityId(), request.getDistrictId(),
                request.getAddress(), request.getContactName(), request.getContactPhone(), request.getContactWechat(),
                request.getDescription(), request.getImages(), request.getExpireDays());
        post.setStatus(PostStatus.DRAFT);
        postMapper.insert(post);

        PostJobSeek jobSeek = new PostJobSeek();
        applyJobSeek(jobSeek, post.getId(), request);
        postJobSeekMapper.insert(jobSeek);
        saveImages(post.getId(), request.getImages());
        return post.getId();
    }

    @Override
    @Transactional
    public Long saveFranchiseDraft(Long userId, FranchisePostUpsertRequest request) {
        ensureActiveUser(userId);
        validateFranchiseRequest(request);
        Post post = buildBasePost(userId, PostType.FRANCHISE, request.getTitle(), request.getCityId(), request.getDistrictId(),
                request.getAddress(), request.getContactName(), request.getContactPhone(), request.getContactWechat(),
                request.getDescription(), request.getImages(), request.getExpireDays());
        post.setStatus(PostStatus.DRAFT);
        postMapper.insert(post);

        PostFranchise franchise = new PostFranchise();
        applyFranchise(franchise, post.getId(), request);
        postFranchiseMapper.insert(franchise);
        saveImages(post.getId(), request.getImages());
        return post.getId();
    }

    @Override
    @Transactional
    public void submitForAudit(Long userId, Long postId) {
        ensureActiveUser(userId);
        Post post = postMapper.selectById(postId);
        if (post == null || !post.getPublisherUserId().equals(userId)) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (post.getStatus() != PostStatus.DRAFT && post.getStatus() != PostStatus.REJECTED) {
            throw new BusinessException(400, "当前状态不可提交审核");
        }
        post.setStatus(PostStatus.PENDING);
        postMapper.updateById(post);
    }

    @Override
    public List<MyPostVO> listMyPosts(Long userId, String status, int page, int size) {
        Page<Post> pageReq = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getPublisherUserId, userId)
                .orderByDesc(Post::getCreatedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Post::getStatus, PostStatus.valueOf(status));
        }
        Page<Post> result = postMapper.selectPage(pageReq, wrapper);
        return result.getRecords().stream().map(p -> MyPostVO.builder()
                .id(String.valueOf(p.getId()))
                .postNo(p.getPostNo())
                .postType(p.getPostType().name())
                .status(p.getStatus().name())
                .title(p.getTitle())
                .coverImage(p.getCoverImage())
                .latestRejectReason(latestRejectReason(p.getId(), p.getStatus()))
                .createdAt(p.getCreatedAt())
                .expireAt(p.getExpireAt())
                .build()).toList();
    }

    @Override
    public Map<String, Object> getEditablePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !post.getPublisherUserId().equals(userId)) {
            throw new BusinessException(404, "帖子不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("images", postImageMapper.selectList(new LambdaQueryWrapper<PostImage>()
                .eq(PostImage::getPostId, postId)
                .orderByAsc(PostImage::getSortNo)));
        if (post.getPostType() == PostType.RECRUIT) {
            result.put("ext", postRecruitMapper.selectById(postId));
        } else if (post.getPostType() == PostType.TRANSFER) {
            result.put("ext", postTransferMapper.selectById(postId));
        } else if (post.getPostType() == PostType.RENT) {
            result.put("ext", postRentMapper.selectById(postId));
        } else if (post.getPostType() == PostType.JOB_SEEK) {
            result.put("ext", postJobSeekMapper.selectById(postId));
        } else if (post.getPostType() == PostType.FRANCHISE) {
            result.put("ext", postFranchiseMapper.selectById(postId));
        }
        return result;
    }

    @Override
    @Transactional
    public void updateRecruitDraft(Long userId, Long postId, RecruitPostUpsertRequest request) {
        ensureActiveUser(userId);
        validateRecruitRequest(request);
        Post post = requireOwnedEditablePost(userId, postId, PostType.RECRUIT);
        applyBasePostUpdate(post, request.getTitle(), request.getCityId(), request.getDistrictId(), request.getAddress(),
                request.getContactName(), request.getContactPhone(), request.getContactWechat(), request.getDescription(),
                request.getImages(), request.getExpireDays());
        postMapper.updateById(post);

        PostRecruit recruit = postRecruitMapper.selectById(postId);
        if (recruit == null) {
            recruit = new PostRecruit();
            recruit.setPostId(postId);
            postRecruitMapper.insert(recruit);
        }
        recruit.setJobRole(request.getJobRole());
        recruit.setJobRoleOther(nvl(request.getJobRoleOther()));
        recruit.setShopCategory(nvl(request.getShopCategory()));
        recruit.setSalaryType(parseSalaryType(request.getSalaryType()));
        recruit.setSalaryMin(request.getSalaryMin());
        recruit.setSalaryMax(request.getSalaryMax());
        recruit.setProvideBoard(defaultInt(request.getProvideBoard(), 0));
        recruit.setHeadcount(defaultInt(request.getHeadcount(), 1));
        recruit.setExpRequirement(nvl(request.getExpRequirement()));
        recruit.setAgeRequirement(nvl(request.getAgeRequirement()));
        recruit.setCuisines(nvl(request.getCuisines()));
        recruit.setWorkTimeDesc(nvl(request.getWorkTimeDesc()));
        postRecruitMapper.updateById(recruit);
        replaceImages(postId, request.getImages());
    }

    @Override
    @Transactional
    public void updateTransferDraft(Long userId, Long postId, TransferPostUpsertRequest request) {
        ensureActiveUser(userId);
        validateTransferRequest(request);
        Post post = requireOwnedEditablePost(userId, postId, PostType.TRANSFER);
        applyBasePostUpdate(post, request.getTitle(), request.getCityId(), request.getDistrictId(), request.getAddress(),
                request.getContactName(), request.getContactPhone(), request.getContactWechat(), request.getDescription(),
                request.getImages(), request.getExpireDays());
        postMapper.updateById(post);

        PostTransfer transfer = postTransferMapper.selectById(postId);
        if (transfer == null) {
            transfer = new PostTransfer();
            transfer.setPostId(postId);
            postTransferMapper.insert(transfer);
        }
        transfer.setShopCategory(request.getShopCategory());
        transfer.setAreaSqm(request.getAreaSqm());
        transfer.setRentMonthly(request.getRentMonthly());
        transfer.setRentNegotiable(defaultInt(request.getRentNegotiable(), 0));
        transfer.setTransferFee(request.getTransferFee());
        transfer.setFeeNegotiable(defaultInt(request.getFeeNegotiable(), 0));
        transfer.setIncludeEquipment(defaultInt(request.getIncludeEquipment(), 0));
        transfer.setOperating(defaultInt(request.getOperating(), 0));
        transfer.setRevenueDesc(nvl(request.getRevenueDesc()));
        transfer.setReason(nvl(request.getReason()));
        postTransferMapper.updateById(transfer);
        replaceImages(postId, request.getImages());
    }

    @Override
    @Transactional
    public void updateRentDraft(Long userId, Long postId, RentPostUpsertRequest request) {
        ensureActiveUser(userId);
        validateRentRequest(request);
        Post post = requireOwnedEditablePost(userId, postId, PostType.RENT);
        applyBasePostUpdate(post, request.getTitle(), request.getCityId(), request.getDistrictId(), request.getAddress(),
                request.getContactName(), request.getContactPhone(), request.getContactWechat(), request.getDescription(),
                request.getImages(), request.getExpireDays());
        postMapper.updateById(post);
        PostRent rent = postRentMapper.selectById(postId);
        if (rent == null) {
            rent = new PostRent();
            rent.setPostId(postId);
            postRentMapper.insert(rent);
        }
        applyRent(rent, postId, request);
        postRentMapper.updateById(rent);
        replaceImages(postId, request.getImages());
    }

    @Override
    @Transactional
    public void updateJobSeekDraft(Long userId, Long postId, JobSeekPostUpsertRequest request) {
        ensureActiveUser(userId);
        validateJobSeekRequest(request);
        Post post = requireOwnedEditablePost(userId, postId, PostType.JOB_SEEK);
        applyBasePostUpdate(post, request.getTitle(), request.getCityId(), request.getDistrictId(), request.getAddress(),
                request.getContactName(), request.getContactPhone(), request.getContactWechat(), request.getDescription(),
                request.getImages(), request.getExpireDays());
        postMapper.updateById(post);
        PostJobSeek jobSeek = postJobSeekMapper.selectById(postId);
        if (jobSeek == null) {
            jobSeek = new PostJobSeek();
            jobSeek.setPostId(postId);
            postJobSeekMapper.insert(jobSeek);
        }
        applyJobSeek(jobSeek, postId, request);
        postJobSeekMapper.updateById(jobSeek);
        replaceImages(postId, request.getImages());
    }

    @Override
    @Transactional
    public void updateFranchiseDraft(Long userId, Long postId, FranchisePostUpsertRequest request) {
        ensureActiveUser(userId);
        validateFranchiseRequest(request);
        Post post = requireOwnedEditablePost(userId, postId, PostType.FRANCHISE);
        applyBasePostUpdate(post, request.getTitle(), request.getCityId(), request.getDistrictId(), request.getAddress(),
                request.getContactName(), request.getContactPhone(), request.getContactWechat(), request.getDescription(),
                request.getImages(), request.getExpireDays());
        postMapper.updateById(post);
        PostFranchise franchise = postFranchiseMapper.selectById(postId);
        if (franchise == null) {
            franchise = new PostFranchise();
            franchise.setPostId(postId);
            postFranchiseMapper.insert(franchise);
        }
        applyFranchise(franchise, postId, request);
        postFranchiseMapper.updateById(franchise);
        replaceImages(postId, request.getImages());
    }

    @Override
    public List<PendingPostVO> listPendingPosts(String postType, int page, int size) {
        Page<Post> pageReq = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, PostStatus.PENDING)
                .orderByAsc(Post::getCreatedAt);
        if (postType != null && !postType.isBlank()) {
            wrapper.eq(Post::getPostType, PostType.valueOf(postType));
        }
        Page<Post> result = postMapper.selectPage(pageReq, wrapper);
        return result.getRecords().stream().map(p -> PendingPostVO.builder()
                .id(String.valueOf(p.getId()))
                .postNo(p.getPostNo())
                .postType(p.getPostType().name())
                .title(p.getTitle())
                .cityId(p.getCityId())
                .districtId(p.getDistrictId())
                .publisherUserId(p.getPublisherUserId())
                .createdAt(p.getCreatedAt())
                .build()).toList();
    }

    @Override
    public Map<String, Object> getPendingPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("images", postImageMapper.selectList(new LambdaQueryWrapper<PostImage>()
                .eq(PostImage::getPostId, postId)
                .orderByAsc(PostImage::getSortNo)));
        result.put("auditHistory", postAuditRecordMapper.selectList(new LambdaQueryWrapper<PostAuditRecord>()
                .eq(PostAuditRecord::getPostId, postId)
                .orderByDesc(PostAuditRecord::getCreatedAt)));
        result.put("publisherRejectedCount", countPublisherRejectedPosts(post.getPublisherUserId(), postId));
        if (post.getPostType() == PostType.RECRUIT) {
            result.put("ext", postRecruitMapper.selectById(postId));
        } else if (post.getPostType() == PostType.TRANSFER) {
            result.put("ext", postTransferMapper.selectById(postId));
        } else if (post.getPostType() == PostType.RENT) {
            result.put("ext", postRentMapper.selectById(postId));
        } else if (post.getPostType() == PostType.JOB_SEEK) {
            result.put("ext", postJobSeekMapper.selectById(postId));
        } else if (post.getPostType() == PostType.FRANCHISE) {
            result.put("ext", postFranchiseMapper.selectById(postId));
        }
        return result;
    }

    @Override
    @Transactional
    public PostAuditRecord auditPendingPost(Long adminUserId, Long postId, PostAuditActionRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (post.getStatus() != PostStatus.PENDING) {
            throw new BusinessException(400, "仅待审核帖子可操作");
        }

        PostStatus targetStatus;
        String action = request.getAction().toUpperCase();
        if ("APPROVE".equals(action)) {
            targetStatus = PostStatus.APPROVED;
        } else if ("REJECT".equals(action)) {
            if (request.getReasonCode() == null || request.getReasonCode().isBlank()) {
                throw new BusinessException(400, "请选择驳回原因");
            }
            targetStatus = PostStatus.REJECTED;
        } else {
            throw new BusinessException(400, "不支持的审核动作");
        }
        post.setStatus(targetStatus);
        if (targetStatus == PostStatus.APPROVED) {
            int expireDays = sysConfigService.getIntValue("post.expire.default_days", 15);
            post.setExpireAt(LocalDateTime.now().plusDays(expireDays));
        }
        postMapper.updateById(post);

        PostAuditRecord record = new PostAuditRecord();
        record.setPostId(postId);
        record.setAction(action);
        record.setReasonCode(nvl(request.getReasonCode()));
        record.setReasonText(nvl(request.getReasonText()));
        record.setOperatorAdminId(adminUserId);
        postAuditRecordMapper.insert(record);
        if (targetStatus == PostStatus.APPROVED) {
            postEsService.syncPost(postId);
        } else {
            postEsService.removePost(postId);
        }
        return record;
    }

    @Override
    public PostPageVO<PostListItemVO> listPublicPosts(String postType, Long cityId, Long districtId,
                                                      String keyword, Integer minSalary, Integer maxSalary,
                                                      String jobRole, String shopCategory,
                                                      Boolean canCatering, Boolean canOpenFlame,
                                                      String sort, int page, int size) {
        expireStaleTopPosts();
        PostSearchQuery query = PostSearchQuery.builder()
                .keyword(keyword)
                .postType(postType)
                .cityId(cityId)
                .districtId(districtId)
                .minSalary(minSalary)
                .maxSalary(maxSalary)
                .jobRole(jobRole)
                .shopCategory(shopCategory)
                .canCatering(canCatering)
                .canOpenFlame(canOpenFlame)
                .sort(normalizeSort(sort))
                .page(safePage(page))
                .size(safeSize(size))
                .build();
        return postEsService.search(query)
                .map(result -> toPageFromEs(result, safePage(page), safeSize(size)))
                .orElseGet(() -> listPublicPostsByDb(query));
    }

    private PostPageVO<PostListItemVO> toPageFromEs(PostSearchResult result, int page, int size) {
        if (result.getIds().isEmpty()) {
            return PostPageVO.<PostListItemVO>builder()
                    .total(0L)
                    .page(page)
                    .size(size)
                    .records(List.of())
                    .build();
        }
        List<Post> posts = postMapper.selectBatchIds(result.getIds());
        Map<Long, Post> byId = posts.stream().collect(java.util.stream.Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
        List<PostListItemVO> records = result.getIds().stream()
                .map(byId::get)
                .filter(java.util.Objects::nonNull)
                .map(this::toListItem)
                .toList();
        return PostPageVO.<PostListItemVO>builder()
                .total(result.getTotal())
                .page(page)
                .size(size)
                .records(records)
                .build();
    }

    private PostPageVO<PostListItemVO> listPublicPostsByDb(PostSearchQuery query) {
        Page<Post> pageReq = new Page<>(query.getPage(), query.getSize());
        List<Long> filteredIds = typeFilterPostIds(query.getPostType(), query.getMinSalary(), query.getMaxSalary(),
                query.getJobRole(), query.getShopCategory(), query.getCanCatering(), query.getCanOpenFlame());
        if (filteredIds != null && filteredIds.isEmpty()) {
            return PostPageVO.<PostListItemVO>builder()
                    .total(0L)
                    .page(query.getPage())
                    .size(query.getSize())
                    .records(List.of())
                    .build();
        }
        LambdaQueryWrapper<Post> wrapper = baseVisiblePostWrapper(query.getPostType(), query.getCityId(),
                query.getDistrictId(), query.getKeyword());
        applyDbSort(wrapper, query.getSort());
        if (filteredIds != null) {
            wrapper.in(Post::getId, filteredIds);
        }
        Page<Post> result = postMapper.selectPage(pageReq, wrapper);
        return PostPageVO.<PostListItemVO>builder()
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .records(result.getRecords().stream().map(this::toListItem).toList())
                .build();
    }

    private void applyDbSort(LambdaQueryWrapper<Post> wrapper, String sort) {
        if ("LATEST".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc(Post::getCreatedAt);
            return;
        }
        if ("EXPIRE_SOON".equalsIgnoreCase(sort)) {
            wrapper.orderByAsc(Post::getExpireAt).orderByDesc(Post::getCreatedAt);
            return;
        }
        wrapper.orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getTopUntil)
                .orderByDesc(Post::getCreatedAt);
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "DEFAULT";
        }
        return sort.trim().toUpperCase();
    }

    @Override
    public PostDetailVO getPublicPostDetail(Long postId, Long viewerUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "信息不存在");
        }
        boolean visible = post.getStatus() == PostStatus.APPROVED
                && post.getExpireAt() != null
                && post.getExpireAt().isAfter(LocalDateTime.now());
        boolean loggedIn = viewerUserId != null;
        boolean activeViewer = loggedIn && isActiveUser(viewerUserId);
        boolean phoneVisible = visible && activeViewer;
        return toDetail(post, visible, phoneVisible, phoneNotice(visible, loggedIn, activeViewer));
    }

    @Override
    public PostDetailVO getAdminPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "信息不存在");
        }
        return toDetail(post, true, true, "运营可查看完整联系方式");
    }

    private PostDetailVO toDetail(Post post, boolean visible, boolean phoneVisible, String phoneNotice) {
        return PostDetailVO.builder()
                .id(String.valueOf(post.getId()))
                .postNo(post.getPostNo())
                .postType(post.getPostType().name())
                .status(post.getStatus().name())
                .title(post.getTitle())
                .cityId(post.getCityId())
                .cityName(regionName(post.getCityId()))
                .districtId(post.getDistrictId())
                .districtName(regionName(post.getDistrictId()))
                .address(post.getAddress())
                .contactName(visible ? post.getContactName() : "")
                .contactPhoneMasked(maskPhone(post.getContactPhone()))
                .contactPhone(phoneVisible ? post.getContactPhone() : "")
                .contactWechat(phoneVisible ? post.getContactWechat() : "")
                .phoneVisible(phoneVisible)
                .phoneNotice(phoneNotice)
                .description(post.getDescription())
                .coverImage(post.getCoverImage())
                .images(postImageUrls(post.getId()))
                .createdAt(post.getCreatedAt())
                .expireAt(post.getExpireAt())
                .ext(extMap(post))
                .build();
    }

    @Override
    public PostPageVO<PostListItemVO> listAdminPosts(String postType, String status, Long cityId,
                                                     String keyword, String phone, int page, int size) {
        expireStaleTopPosts();
        Page<Post> pageReq = new Page<>(safePage(page), safeSize(size));
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>().orderByDesc(Post::getCreatedAt);
        if (postType != null && !postType.isBlank()) {
            wrapper.eq(Post::getPostType, PostType.valueOf(postType));
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(Post::getStatus, PostStatus.valueOf(status));
        }
        if (cityId != null) {
            wrapper.eq(Post::getCityId, cityId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getDescription, keyword).or().like(Post::getPostNo, keyword));
        }
        if (phone != null && !phone.isBlank()) {
            wrapper.like(Post::getContactPhone, phone);
        }
        Page<Post> result = postMapper.selectPage(pageReq, wrapper);
        return PostPageVO.<PostListItemVO>builder()
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .records(result.getRecords().stream().map(this::toListItem).toList())
                .build();
    }

    @Override
    @Transactional
    public void setTop(Long postId, PostTopRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "信息不存在");
        }
        if (post.getStatus() != PostStatus.APPROVED) {
            throw new BusinessException(400, "仅已通过信息可置顶");
        }
        int days = request == null || request.getTopDays() == null ? 7 : request.getTopDays();
        if (days < 1 || days > 30) {
            throw new BusinessException(400, "置顶天数需在 1-30 天之间");
        }
        post.setIsTop(1);
        post.setTopUntil(LocalDateTime.now().plusDays(days));
        postMapper.updateById(post);
        postEsService.syncPost(postId);
    }

    @Override
    @Transactional
    public void cancelTop(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "信息不存在");
        }
        post.setIsTop(0);
        post.setTopUntil(null);
        postMapper.updateById(post);
        postEsService.syncPost(postId);
    }

    private long countPublisherRejectedPosts(Long publisherUserId, Long currentPostId) {
        if (publisherUserId == null) {
            return 0;
        }
        return postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getPublisherUserId, publisherUserId)
                .eq(Post::getStatus, PostStatus.REJECTED)
                .ne(Post::getId, currentPostId));
    }

    private String latestRejectReason(Long postId, PostStatus status) {
        if (status != PostStatus.REJECTED) {
            return "";
        }
        PostAuditRecord record = postAuditRecordMapper.selectOne(new LambdaQueryWrapper<PostAuditRecord>()
                .eq(PostAuditRecord::getPostId, postId)
                .eq(PostAuditRecord::getAction, "REJECT")
                .orderByDesc(PostAuditRecord::getCreatedAt)
                .last("LIMIT 1"));
        if (record == null) {
            return "";
        }
        return record.getReasonText() == null || record.getReasonText().isBlank()
                ? record.getReasonCode()
                : record.getReasonText();
    }

    private LambdaQueryWrapper<Post> baseVisiblePostWrapper(String postType, Long cityId, Long districtId, String keyword) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, PostStatus.APPROVED)
                .gt(Post::getExpireAt, LocalDateTime.now());
        if (postType != null && !postType.isBlank()) {
            wrapper.eq(Post::getPostType, PostType.valueOf(postType));
        }
        if (cityId != null) {
            wrapper.eq(Post::getCityId, cityId);
        }
        if (districtId != null) {
            wrapper.eq(Post::getDistrictId, districtId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword)
                    .or().like(Post::getDescription, keyword)
                    .or().like(Post::getPostNo, keyword)
                    .or().like(Post::getAddress, keyword));
        }
        return wrapper;
    }

    private void expireStaleTopPosts() {
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getIsTop, 1)
                .le(Post::getTopUntil, LocalDateTime.now())
                .set(Post::getIsTop, 0)
                .set(Post::getTopUntil, null));
    }

    private List<Long> typeFilterPostIds(String postType, Integer minSalary, Integer maxSalary, String jobRole,
                                         String shopCategory, Boolean canCatering, Boolean canOpenFlame) {
        if (postType == null || postType.isBlank() || !hasTypeFilter(minSalary, maxSalary, jobRole, shopCategory, canCatering, canOpenFlame)) {
            return null;
        }
        PostType type = PostType.valueOf(postType);
        if (type == PostType.RECRUIT) {
            LambdaQueryWrapper<PostRecruit> wrapper = new LambdaQueryWrapper<>();
            if (jobRole != null && !jobRole.isBlank()) {
                wrapper.eq(PostRecruit::getJobRole, jobRole);
            }
            if (shopCategory != null && !shopCategory.isBlank()) {
                wrapper.eq(PostRecruit::getShopCategory, shopCategory);
            }
            applySalaryOverlap(wrapper, PostRecruit::getSalaryMin, PostRecruit::getSalaryMax, minSalary, maxSalary);
            return postRecruitMapper.selectList(wrapper).stream().map(PostRecruit::getPostId).toList();
        }
        if (type == PostType.TRANSFER) {
            LambdaQueryWrapper<PostTransfer> wrapper = new LambdaQueryWrapper<>();
            if (shopCategory != null && !shopCategory.isBlank()) {
                wrapper.eq(PostTransfer::getShopCategory, shopCategory);
            }
            return postTransferMapper.selectList(wrapper).stream().map(PostTransfer::getPostId).toList();
        }
        if (type == PostType.RENT) {
            LambdaQueryWrapper<PostRent> wrapper = new LambdaQueryWrapper<>();
            if (minSalary != null) {
                wrapper.ge(PostRent::getRentMonthly, minSalary);
            }
            if (maxSalary != null) {
                wrapper.le(PostRent::getRentMonthly, maxSalary);
            }
            if (canCatering != null) {
                wrapper.eq(PostRent::getCanCatering, canCatering ? 1 : 0);
            }
            if (canOpenFlame != null) {
                wrapper.eq(PostRent::getCanOpenFlame, canOpenFlame ? 1 : 0);
            }
            return postRentMapper.selectList(wrapper).stream().map(PostRent::getPostId).toList();
        }
        if (type == PostType.JOB_SEEK) {
            LambdaQueryWrapper<PostJobSeek> wrapper = new LambdaQueryWrapper<>();
            if (jobRole != null && !jobRole.isBlank()) {
                wrapper.like(PostJobSeek::getDesiredRoles, jobRole);
            }
            if (shopCategory != null && !shopCategory.isBlank()) {
                wrapper.like(PostJobSeek::getCuisines, shopCategory);
            }
            applySalaryOverlap(wrapper, PostJobSeek::getSalaryMin, PostJobSeek::getSalaryMax, minSalary, maxSalary);
            return postJobSeekMapper.selectList(wrapper).stream().map(PostJobSeek::getPostId).toList();
        }
        if (type == PostType.FRANCHISE) {
            LambdaQueryWrapper<PostFranchise> wrapper = new LambdaQueryWrapper<>();
            if (jobRole != null && !jobRole.isBlank()) {
                wrapper.like(PostFranchise::getBrandName, jobRole);
            }
            if (shopCategory != null && !shopCategory.isBlank()) {
                wrapper.like(PostFranchise::getCategory, shopCategory);
            }
            return postFranchiseMapper.selectList(wrapper).stream().map(PostFranchise::getPostId).toList();
        }
        return null;
    }

    private boolean hasTypeFilter(Integer minSalary, Integer maxSalary, String jobRole, String shopCategory,
                                  Boolean canCatering, Boolean canOpenFlame) {
        return minSalary != null
                || maxSalary != null
                || (jobRole != null && !jobRole.isBlank())
                || (shopCategory != null && !shopCategory.isBlank())
                || canCatering != null
                || canOpenFlame != null;
    }

    private <T> void applySalaryOverlap(LambdaQueryWrapper<T> wrapper,
                                        com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, Integer> salaryMinColumn,
                                        com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, Integer> salaryMaxColumn,
                                        Integer minSalary, Integer maxSalary) {
        if (minSalary != null) {
            wrapper.and(w -> w.ge(salaryMaxColumn, minSalary).or().isNull(salaryMaxColumn));
        }
        if (maxSalary != null) {
            wrapper.and(w -> w.le(salaryMinColumn, maxSalary).or().isNull(salaryMinColumn));
        }
    }

    private PostListItemVO toListItem(Post post) {
        return PostListItemVO.builder()
                .id(String.valueOf(post.getId()))
                .postNo(post.getPostNo())
                .postType(post.getPostType().name())
                .status(post.getStatus().name())
                .title(post.getTitle())
                .cityId(post.getCityId())
                .cityName(regionName(post.getCityId()))
                .districtId(post.getDistrictId())
                .districtName(regionName(post.getDistrictId()))
                .summary(summary(post))
                .coverImage(post.getCoverImage())
                .isTop(activeTop(post) ? 1 : 0)
                .topUntil(post.getTopUntil())
                .createdAt(post.getCreatedAt())
                .expireAt(post.getExpireAt())
                .highlights(extMap(post))
                .build();
    }

    private boolean activeTop(Post post) {
        return post.getIsTop() != null && post.getIsTop() == 1
                && post.getTopUntil() != null
                && post.getTopUntil().isAfter(LocalDateTime.now());
    }

    private String summary(Post post) {
        Map<String, Object> ext = extMap(post);
        if (post.getPostType() == PostType.RECRUIT) {
            return String.join(" | ",
                    nonBlank(regionName(post.getDistrictId()), "未选区县"),
                    nonBlank(String.valueOf(ext.getOrDefault("jobRole", "")), "岗位待补充"),
                    salaryText(ext),
                    Boolean.TRUE.equals(ext.get("provideBoard")) ? "包吃住" : "不含吃住");
        }
        if (post.getPostType() == PostType.TRANSFER) {
            return String.join(" | ",
                    nonBlank(regionName(post.getDistrictId()), "未选区县"),
                    nonBlank(String.valueOf(ext.getOrDefault("shopCategory", "")), "经营类型待补充"),
                    ext.getOrDefault("areaSqm", "-") + "㎡",
                    feeText(ext, "transferFee", "feeNegotiable", "转让费"));
        }
        if (post.getPostType() == PostType.RENT) {
            return String.join(" | ",
                    nonBlank(regionName(post.getDistrictId()), "未选区县"),
                    ext.getOrDefault("areaSqm", "-") + "㎡",
                    feeText(ext, "rentMonthly", "rentNegotiable", "月租"),
                    Boolean.TRUE.equals(ext.get("canCatering")) ? "可餐饮" : "不可餐饮");
        }
        if (post.getPostType() == PostType.JOB_SEEK) {
            return String.join(" | ",
                    nonBlank(regionName(post.getDistrictId()), "未选区县"),
                    nonBlank(String.valueOf(ext.getOrDefault("desiredRoles", "")), "期望岗位待补充"),
                    salaryRangeText(ext));
        }
        if (post.getPostType() == PostType.FRANCHISE) {
            return String.join(" | ",
                    nonBlank(regionName(post.getDistrictId()), "未选区县"),
                    nonBlank(String.valueOf(ext.getOrDefault("brandName", "")), "品牌待补充"),
                    nonBlank(String.valueOf(ext.getOrDefault("category", "")), "品类待补充"));
        }
        return post.getDescription() == null || post.getDescription().isBlank()
                ? regionName(post.getDistrictId())
                : post.getDescription();
    }

    private Map<String, Object> extMap(Post post) {
        Map<String, Object> ext = new HashMap<>();
        if (post.getPostType() == PostType.RECRUIT) {
            PostRecruit recruit = postRecruitMapper.selectById(post.getId());
            if (recruit != null) {
                ext.put("jobRole", recruit.getJobRole());
                ext.put("jobRoleOther", recruit.getJobRoleOther());
                ext.put("shopCategory", recruit.getShopCategory());
                ext.put("salaryType", recruit.getSalaryType() == null ? "" : recruit.getSalaryType().name());
                ext.put("salaryMin", recruit.getSalaryMin());
                ext.put("salaryMax", recruit.getSalaryMax());
                ext.put("provideBoard", recruit.getProvideBoard() != null && recruit.getProvideBoard() == 1);
                ext.put("headcount", recruit.getHeadcount());
                ext.put("expRequirement", recruit.getExpRequirement());
                ext.put("cuisines", recruit.getCuisines());
                ext.put("workTimeDesc", recruit.getWorkTimeDesc());
            }
        } else if (post.getPostType() == PostType.TRANSFER) {
            PostTransfer transfer = postTransferMapper.selectById(post.getId());
            if (transfer != null) {
                ext.put("shopCategory", transfer.getShopCategory());
                ext.put("areaSqm", transfer.getAreaSqm());
                ext.put("rentMonthly", transfer.getRentMonthly());
                ext.put("rentNegotiable", transfer.getRentNegotiable() != null && transfer.getRentNegotiable() == 1);
                ext.put("transferFee", transfer.getTransferFee());
                ext.put("feeNegotiable", transfer.getFeeNegotiable() != null && transfer.getFeeNegotiable() == 1);
                ext.put("includeEquipment", transfer.getIncludeEquipment() != null && transfer.getIncludeEquipment() == 1);
                ext.put("operating", transfer.getOperating() != null && transfer.getOperating() == 1);
                ext.put("revenueDesc", transfer.getRevenueDesc());
                ext.put("reason", transfer.getReason());
            }
        } else if (post.getPostType() == PostType.RENT) {
            PostRent rent = postRentMapper.selectById(post.getId());
            if (rent != null) {
                ext.put("areaSqm", rent.getAreaSqm());
                ext.put("rentMonthly", rent.getRentMonthly());
                ext.put("rentNegotiable", rent.getRentNegotiable() != null && rent.getRentNegotiable() == 1);
                ext.put("entryFee", rent.getEntryFee());
                ext.put("entryFeeNegotiable", rent.getEntryFeeNegotiable() != null && rent.getEntryFeeNegotiable() == 1);
                ext.put("canCatering", rent.getCanCatering() != null && rent.getCanCatering() == 1);
                ext.put("canOpenFlame", rent.getCanOpenFlame() != null && rent.getCanOpenFlame() == 1);
                ext.put("floorDesc", rent.getFloorDesc());
                ext.put("publisherIdentity", rent.getPublisherIdentity() == null ? "" : rent.getPublisherIdentity().name());
            }
        } else if (post.getPostType() == PostType.JOB_SEEK) {
            PostJobSeek jobSeek = postJobSeekMapper.selectById(post.getId());
            if (jobSeek != null) {
                ext.put("desiredRoles", jobSeek.getDesiredRoles());
                ext.put("desiredCities", jobSeek.getDesiredCities());
                ext.put("desiredDistricts", jobSeek.getDesiredDistricts());
                ext.put("workYears", jobSeek.getWorkYears());
                ext.put("cuisines", jobSeek.getCuisines());
                ext.put("salaryMin", jobSeek.getSalaryMin());
                ext.put("salaryMax", jobSeek.getSalaryMax());
                ext.put("gender", jobSeek.getGender());
                ext.put("age", jobSeek.getAge());
                ext.put("intro", jobSeek.getIntro());
            }
        } else if (post.getPostType() == PostType.FRANCHISE) {
            PostFranchise franchise = postFranchiseMapper.selectById(post.getId());
            if (franchise != null) {
                ext.put("brandName", franchise.getBrandName());
                ext.put("category", franchise.getCategory());
                ext.put("investmentDesc", franchise.getInvestmentDesc());
                ext.put("franchiseDesc", franchise.getFranchiseDesc());
            }
        }
        return ext;
    }

    private String salaryText(Map<String, Object> ext) {
        if ("NEGOTIABLE".equals(ext.get("salaryType"))) {
            return "薪资面议";
        }
        Object min = ext.get("salaryMin");
        Object max = ext.get("salaryMax");
        if (min == null) {
            return "薪资待补充";
        }
        return max == null ? min + "元起" : min + "-" + max + "元";
    }

    private String feeText(Map<String, Object> ext, String feeKey, String negotiableKey, String label) {
        if (Boolean.TRUE.equals(ext.get(negotiableKey))) {
            return label + "面议";
        }
        Object value = ext.get(feeKey);
        return value == null ? label + "待补充" : label + value + "元";
    }

    private String salaryRangeText(Map<String, Object> ext) {
        Object min = ext.get("salaryMin");
        Object max = ext.get("salaryMax");
        if (min == null && max == null) {
            return "薪资面议";
        }
        if (max == null) {
            return min + "元起";
        }
        if (min == null) {
            return max + "元以内";
        }
        return min + "-" + max + "元";
    }

    private List<String> postImageUrls(Long postId) {
        return postImageMapper.selectList(new LambdaQueryWrapper<PostImage>()
                        .eq(PostImage::getPostId, postId)
                        .orderByAsc(PostImage::getSortNo))
                .stream()
                .map(PostImage::getUrl)
                .toList();
    }

    private String regionName(Long regionId) {
        if (regionId == null) {
            return "";
        }
        SysRegion region = sysRegionMapper.selectById(regionId);
        return region == null ? String.valueOf(regionId) : region.getName();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String phoneNotice(boolean visible, boolean loggedIn, boolean activeViewer) {
        if (!visible) {
            return "信息已失效，暂不可查看电话";
        }
        if (!loggedIn) {
            return "登录后查看完整电话";
        }
        return activeViewer ? "已登录，可拨打电话" : "账号已被封禁，暂不可查看电话";
    }

    private void ensureActiveUser(Long userId) {
        if (!isActiveUser(userId)) {
            throw new BusinessException(403, "账号已被封禁，暂不能发布或编辑信息");
        }
    }

    private boolean isActiveUser(Long userId) {
        if (userId == null) {
            return false;
        }
        AppUser user = appUserMapper.selectById(userId);
        return user != null && (user.getBannedUntil() == null || !user.getBannedUntil().isAfter(LocalDateTime.now()));
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int safePage(int page) {
        return Math.max(page, 1);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 50);
    }

    private void validateRecruitRequest(RecruitPostUpsertRequest request) {
        validateBaseRequest(request.getTitle(), request.getContactName(), request.getContactPhone(),
                request.getExpireDays(), request.getImages());
        if (request.getJobRole() == null || request.getJobRole().isBlank()) {
            throw new BusinessException(400, "岗位不能为空");
        }
        if ("OTHER".equalsIgnoreCase(request.getJobRole())
                && (request.getJobRoleOther() == null || request.getJobRoleOther().isBlank())) {
            throw new BusinessException(400, "选择其他岗位时请填写岗位名称");
        }
        SalaryType salaryType = parseSalaryType(request.getSalaryType());
        if (salaryType == SalaryType.MONTHLY) {
            if (request.getSalaryMin() == null || request.getSalaryMin() <= 0) {
                throw new BusinessException(400, "固定月薪请填写薪资下限");
            }
            if (request.getSalaryMax() != null && request.getSalaryMax() < request.getSalaryMin()) {
                throw new BusinessException(400, "薪资上限不能低于薪资下限");
            }
        }
        if (request.getHeadcount() != null && (request.getHeadcount() < 1 || request.getHeadcount() > 99)) {
            throw new BusinessException(400, "招聘人数需在 1-99 之间");
        }
    }

    private void validateTransferRequest(TransferPostUpsertRequest request) {
        validateBaseRequest(request.getTitle(), request.getContactName(), request.getContactPhone(),
                request.getExpireDays(), request.getImages());
        if (request.getShopCategory() == null || request.getShopCategory().isBlank()) {
            throw new BusinessException(400, "经营类型不能为空");
        }
        if (request.getAreaSqm() == null || request.getAreaSqm() <= 0) {
            throw new BusinessException(400, "面积必须大于 0");
        }
        if (defaultInt(request.getRentNegotiable(), 0) == 0
                && (request.getRentMonthly() == null || request.getRentMonthly() <= 0)) {
            throw new BusinessException(400, "请填写月租金，或选择月租面议");
        }
        if (defaultInt(request.getFeeNegotiable(), 0) == 0
                && (request.getTransferFee() == null || request.getTransferFee() < 0)) {
            throw new BusinessException(400, "请填写转让费，或选择转让费面议");
        }
        if (request.getIncludeEquipment() == null) {
            throw new BusinessException(400, "请选择是否带设备");
        }
        if (request.getOperating() == null) {
            throw new BusinessException(400, "请选择是否营业中");
        }
    }

    private void validateRentRequest(RentPostUpsertRequest request) {
        validateBaseRequest(request.getTitle(), request.getContactName(), request.getContactPhone(),
                request.getExpireDays(), request.getImages());
        if (request.getAreaSqm() == null || request.getAreaSqm() <= 0) {
            throw new BusinessException(400, "面积必须大于 0");
        }
        if (defaultInt(request.getRentNegotiable(), 0) == 0
                && (request.getRentMonthly() == null || request.getRentMonthly() <= 0)) {
            throw new BusinessException(400, "请填写月租金，或选择月租面议");
        }
        if (request.getCanCatering() == null) {
            throw new BusinessException(400, "请选择是否可餐饮");
        }
        if (request.getCanOpenFlame() == null) {
            throw new BusinessException(400, "请选择是否可明火");
        }
        parsePublisherIdentity(request.getPublisherIdentity());
    }

    private void validateJobSeekRequest(JobSeekPostUpsertRequest request) {
        validateBaseRequest(request.getTitle(), request.getContactName(), request.getContactPhone(),
                request.getExpireDays(), request.getImages());
        if (request.getDesiredRoles() == null || request.getDesiredRoles().isBlank()) {
            throw new BusinessException(400, "期望岗位不能为空");
        }
        if (request.getSalaryMin() != null && request.getSalaryMax() != null && request.getSalaryMax() < request.getSalaryMin()) {
            throw new BusinessException(400, "期望薪资上限不能低于下限");
        }
        if (request.getAge() != null && (request.getAge() < 16 || request.getAge() > 80)) {
            throw new BusinessException(400, "年龄需在 16-80 之间");
        }
    }

    private void validateFranchiseRequest(FranchisePostUpsertRequest request) {
        validateBaseRequest(request.getTitle(), request.getContactName(), request.getContactPhone(),
                request.getExpireDays(), request.getImages());
        if (request.getBrandName() == null || request.getBrandName().trim().length() < 2) {
            throw new BusinessException(400, "品牌/项目名称不能为空");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new BusinessException(400, "品类不能为空");
        }
        if (request.getFranchiseDesc() == null || request.getFranchiseDesc().trim().length() < 10) {
            throw new BusinessException(400, "加盟说明至少 10 个字");
        }
    }

    private void applyRent(PostRent rent, Long postId, RentPostUpsertRequest request) {
        rent.setPostId(postId);
        rent.setAreaSqm(request.getAreaSqm());
        rent.setRentMonthly(request.getRentMonthly());
        rent.setRentNegotiable(defaultInt(request.getRentNegotiable(), 0));
        rent.setEntryFee(request.getEntryFee());
        rent.setEntryFeeNegotiable(defaultInt(request.getEntryFeeNegotiable(), 0));
        rent.setCanCatering(defaultInt(request.getCanCatering(), 0));
        rent.setCanOpenFlame(defaultInt(request.getCanOpenFlame(), 0));
        rent.setFloorDesc(nvl(request.getFloorDesc()));
        rent.setPublisherIdentity(parsePublisherIdentity(request.getPublisherIdentity()));
    }

    private void applyJobSeek(PostJobSeek jobSeek, Long postId, JobSeekPostUpsertRequest request) {
        jobSeek.setPostId(postId);
        jobSeek.setDesiredRoles(request.getDesiredRoles());
        jobSeek.setDesiredCities(nvl(request.getDesiredCities()));
        jobSeek.setDesiredDistricts(nvl(request.getDesiredDistricts()));
        jobSeek.setWorkYears(request.getWorkYears());
        jobSeek.setCuisines(nvl(request.getCuisines()));
        jobSeek.setSalaryMin(request.getSalaryMin());
        jobSeek.setSalaryMax(request.getSalaryMax());
        jobSeek.setGender(nvl(request.getGender()));
        jobSeek.setAge(request.getAge());
        jobSeek.setIntro(nvl(request.getIntro()));
    }

    private void applyFranchise(PostFranchise franchise, Long postId, FranchisePostUpsertRequest request) {
        franchise.setPostId(postId);
        franchise.setBrandName(request.getBrandName());
        franchise.setCategory(request.getCategory());
        franchise.setInvestmentDesc(nvl(request.getInvestmentDesc()));
        franchise.setFranchiseDesc(request.getFranchiseDesc());
    }

    private void validateBaseRequest(String title, String contactName, String contactPhone,
                                     Integer expireDays, List<String> images) {
        int titleLength = title == null ? 0 : title.trim().length();
        if (titleLength < MIN_TITLE_LENGTH || titleLength > MAX_TITLE_LENGTH) {
            throw new BusinessException(400, "标题需为 5-30 个字");
        }
        if (contactName == null || contactName.trim().length() < 2 || contactName.trim().length() > 20) {
            throw new BusinessException(400, "联系人需为 2-20 个字");
        }
        if (contactPhone == null || !PHONE_PATTERN.matcher(contactPhone).matches()) {
            throw new BusinessException(400, "请输入 11 位手机号");
        }
        if (expireDays != null && expireDays != 7 && expireDays != 15 && expireDays != 30) {
            throw new BusinessException(400, "有效期只能选择 7/15/30 天");
        }
        if (images != null && images.size() > 9) {
            throw new BusinessException(400, "图片最多上传 9 张");
        }
    }

    private SalaryType parseSalaryType(String raw) {
        try {
            return SalaryType.valueOf(raw == null ? "" : raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "薪资类型不正确");
        }
    }

    private PublisherIdentity parsePublisherIdentity(String raw) {
        try {
            return PublisherIdentity.valueOf(raw == null || raw.isBlank() ? "OWNER" : raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "发布方身份不正确");
        }
    }

    private Post buildBasePost(Long userId, PostType postType, String title, Long cityId, Long districtId, String address,
                               String contactName, String contactPhone, String contactWechat, String description,
                               List<String> images, Integer expireDays) {
        Post post = new Post();
        post.setPostNo(generatePostNo());
        post.setPostType(postType);
        post.setTitle(title);
        post.setProvinceId(330000L);
        post.setCityId(cityId);
        post.setDistrictId(districtId);
        post.setAddress(nvl(address));
        post.setContactName(contactName);
        post.setContactPhone(contactPhone);
        post.setContactWechat(nvl(contactWechat));
        post.setDescription(nvl(description));
        post.setCoverImage(images == null || images.isEmpty() ? "" : images.get(0));
        post.setImagesCount(images == null ? 0 : images.size());
        post.setExpireAt(LocalDateTime.now().plusDays(expireDays == null || expireDays <= 0 ? 15 : expireDays));
        post.setRenewCount(0);
        post.setIsTop(0);
        post.setPublisherUserId(userId);
        post.setVerifiedTag(0);
        return post;
    }

    private void saveImages(Long postId, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            PostImage image = new PostImage();
            image.setPostId(postId);
            image.setUrl(images.get(i));
            image.setSortNo(i);
            postImageMapper.insert(image);
        }
    }

    private void replaceImages(Long postId, List<String> images) {
        postImageMapper.delete(new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, postId));
        saveImages(postId, images);
    }

    private Post requireOwnedEditablePost(Long userId, Long postId, PostType expectedType) {
        Post post = postMapper.selectById(postId);
        if (post == null || !post.getPublisherUserId().equals(userId)) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (post.getPostType() != expectedType) {
            throw new BusinessException(400, "帖子类型不匹配");
        }
        if (post.getStatus() != PostStatus.DRAFT && post.getStatus() != PostStatus.REJECTED) {
            throw new BusinessException(400, "当前状态不允许编辑");
        }
        return post;
    }

    private void applyBasePostUpdate(Post post, String title, Long cityId, Long districtId, String address,
                                     String contactName, String contactPhone, String contactWechat, String description,
                                     List<String> images, Integer expireDays) {
        post.setTitle(title);
        post.setCityId(cityId);
        post.setDistrictId(districtId);
        post.setAddress(nvl(address));
        post.setContactName(contactName);
        post.setContactPhone(contactPhone);
        post.setContactWechat(nvl(contactWechat));
        post.setDescription(nvl(description));
        post.setCoverImage(images == null || images.isEmpty() ? "" : images.get(0));
        post.setImagesCount(images == null ? 0 : images.size());
        post.setExpireAt(LocalDateTime.now().plusDays(expireDays == null || expireDays <= 0 ? 15 : expireDays));
    }

    private String generatePostNo() {
        return "P" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
