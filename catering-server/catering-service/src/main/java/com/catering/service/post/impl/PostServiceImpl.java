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

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;
    private final PostRecruitMapper postRecruitMapper;
    private final PostTransferMapper postTransferMapper;
    private final PostImageMapper postImageMapper;
    private final PostAuditRecordMapper postAuditRecordMapper;
    private final SysConfigService sysConfigService;

    @Override
    @Transactional
    public Long saveRecruitDraft(Long userId, RecruitPostUpsertRequest request) {
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
        recruit.setSalaryType(SalaryType.valueOf(request.getSalaryType()));
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
                .id(p.getId())
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

