package com.catering.service.post;

import com.baomidou.mybatisplus.extension.service.IService;
import com.catering.model.entity.PostAuditRecord;
import com.catering.model.entity.Post;
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

import java.util.List;
import java.util.Map;

public interface PostService extends IService<Post> {
    Long saveRecruitDraft(Long userId, RecruitPostUpsertRequest request);

    Long saveTransferDraft(Long userId, TransferPostUpsertRequest request);

    Long saveRentDraft(Long userId, RentPostUpsertRequest request);

    Long saveJobSeekDraft(Long userId, JobSeekPostUpsertRequest request);

    Long saveFranchiseDraft(Long userId, FranchisePostUpsertRequest request);

    void submitForAudit(Long userId, Long postId);

    List<MyPostVO> listMyPosts(Long userId, String status, int page, int size);

    java.util.Map<String, Object> getEditablePost(Long userId, Long postId);

    void updateRecruitDraft(Long userId, Long postId, RecruitPostUpsertRequest request);

    void updateTransferDraft(Long userId, Long postId, TransferPostUpsertRequest request);

    void updateRentDraft(Long userId, Long postId, RentPostUpsertRequest request);

    void updateJobSeekDraft(Long userId, Long postId, JobSeekPostUpsertRequest request);

    void updateFranchiseDraft(Long userId, Long postId, FranchisePostUpsertRequest request);

    List<PendingPostVO> listPendingPosts(String postType, int page, int size);

    Map<String, Object> getPendingPostDetail(Long postId);

    PostAuditRecord auditPendingPost(Long adminUserId, Long postId, PostAuditActionRequest request);

    PostPageVO<PostListItemVO> listPublicPosts(String postType, Long cityId, Long districtId,
                                               String keyword, Integer minSalary, Integer maxSalary,
                                               String jobRole, String shopCategory,
                                               Boolean canCatering, Boolean canOpenFlame,
                                               int page, int size);

    PostDetailVO getPublicPostDetail(Long postId, Long viewerUserId);

    PostDetailVO getAdminPostDetail(Long postId);

    PostPageVO<PostListItemVO> listAdminPosts(String postType, String status, Long cityId,
                                              String keyword, String phone, int page, int size);

    void setTop(Long postId, PostTopRequest request);

    void cancelTop(Long postId);
}
