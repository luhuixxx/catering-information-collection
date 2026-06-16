package com.catering.service.post.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.common.exception.BusinessException;
import com.catering.dao.mapper.PostAuditRecordMapper;
import com.catering.dao.mapper.PostImageMapper;
import com.catering.dao.mapper.PostMapper;
import com.catering.dao.mapper.PostRecruitMapper;
import com.catering.dao.mapper.PostTransferMapper;
import com.catering.model.entity.PostAuditRecord;
import com.catering.model.entity.PostImage;
import com.catering.model.entity.Post;
import com.catering.model.entity.PostRecruit;
import com.catering.model.entity.PostTransfer;
import com.catering.model.enums.PostStatus;
import com.catering.model.enums.PostType;
import com.catering.model.enums.SalaryType;
import com.catering.service.config.SysConfigService;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.MyPostVO;
import com.catering.service.post.dto.PendingPostVO;
import com.catering.service.post.dto.PostAuditActionRequest;
import com.catering.service.post.dto.RecruitPostUpsertRequest;
import com.catering.service.post.dto.TransferPostUpsertRequest;
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
    private final PostImageMapper postImageMapper;
    private final PostAuditRecordMapper postAuditRecordMapper;
    private final SysConfigService sysConfigService;

    @Override
    @Transactional
    public Long saveRecruitDraft(Long userId, RecruitPostUpsertRequest request) {
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
    public void submitForAudit(Long userId, Long postId) {
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
        }
        return result;
    }

    @Override
    @Transactional
    public void updateRecruitDraft(Long userId, Long postId, RecruitPostUpsertRequest request) {
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
        return record;
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
