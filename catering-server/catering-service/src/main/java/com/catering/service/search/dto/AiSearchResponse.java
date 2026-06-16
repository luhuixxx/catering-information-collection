package com.catering.service.search.dto;

import com.catering.service.post.dto.PostListItemVO;
import com.catering.service.post.dto.PostPageVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiSearchResponse {

    private String reply;
    private boolean degraded;
    private Double confidence;
    private PostSearchQuery parsedFilters;
    private PostPageVO<PostListItemVO> list;
    private List<PostListItemVO> cards;
    private String messageType;
}
