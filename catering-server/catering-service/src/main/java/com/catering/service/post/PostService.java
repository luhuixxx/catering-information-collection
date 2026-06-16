package com.catering.service.post;

import com.baomidou.mybatisplus.extension.service.IService;
import com.catering.model.entity.PostAuditRecord;
import com.catering.model.entity.Post;
import com.catering.service.post.dto.MyPostVO;
import com.catering.service.post.dto.PendingPostVO;
import com.catering.service.post.dto.PostAuditActionRequest;
import com.catering.service.post.dto.RecruitPostUpsertRequest;
import com.catering.service.post.dto.TransferPostUpsertRequest;

import java.util.List;
import java.util.Map;

public interface PostService extends IService<Post> {
    Long saveRecruitDraft(Long userId, RecruitPostUpsertRequest request);

    Long saveTransferDraft(Long userId, TransferPostUpsertRequest request);

    void submitForAudit(Long userId, Long postId);

    List<MyPostVO> listMyPosts(Long userId, String status, int page, int size);

    java.util.Map<String, Object> getEditablePost(Long userId, Long postId);

    void updateRecruitDraft(Long userId, Long postId, RecruitPostUpsertRequest request);

    void updateTransferDraft(Long userId, Long postId, TransferPostUpsertRequest request);

    List<PendingPostVO> listPendingPosts(String postType, int page, int size);

    Map<String, Object> getPendingPostDetail(Long postId);

    PostAuditRecord auditPendingPost(Long adminUserId, Long postId, PostAuditActionRequest request);
}

