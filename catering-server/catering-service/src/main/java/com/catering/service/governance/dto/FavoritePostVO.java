package com.catering.service.governance.dto;

import com.catering.service.post.dto.PostListItemVO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FavoritePostVO {
    private String favoriteId;
    private LocalDateTime favoritedAt;
    private PostListItemVO post;
}
