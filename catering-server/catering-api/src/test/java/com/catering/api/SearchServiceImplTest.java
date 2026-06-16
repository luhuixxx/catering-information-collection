package com.catering.api;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.catering.ai.dto.PostSearchFilter;
import com.catering.ai.dto.SearchParseResponse;
import com.catering.ai.exception.AiDegradedException;
import com.catering.ai.facade.AiFacade;
import com.catering.dao.mapper.SysRegionMapper;
import com.catering.model.entity.SysRegion;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.PostListItemVO;
import com.catering.service.post.dto.PostPageVO;
import com.catering.service.search.dto.AiSearchRequest;
import com.catering.service.search.dto.AiSearchResponse;
import com.catering.service.search.impl.SearchServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchServiceImplTest {

    @Test
    void shouldPreferAiParsedCityNameOverPageDefaultCity() {
        AiFacade aiFacade = mock(AiFacade.class);
        PostService postService = mock(PostService.class);
        SysRegionMapper regionMapper = mock(SysRegionMapper.class);
        SearchServiceImpl service = new SearchServiceImpl(aiFacade, postService, regionMapper);

        SearchParseResponse parsed = new SearchParseResponse();
        parsed.setIntent("search");
        PostSearchFilter filter = new PostSearchFilter();
        filter.setCityName("杭州市");
        filter.setJobRole("大厨");
        parsed.setFilters(filter);
        when(aiFacade.parseSearchQuery(any())).thenReturn(parsed);
        when(regionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(region(330100L, 2, "杭州市"), region(330200L, 2, "宁波市")));
        when(postService.listPublicPosts(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(PostPageVO.<PostListItemVO>builder().page(1).size(8).total(0).records(List.of()).build());

        AiSearchRequest request = new AiSearchRequest();
        request.setQuery("杭州大厨 8000 以上");
        request.setCityId(330200L);
        request.setPage(1);
        request.setSize(8);

        AiSearchResponse response = service.aiSearch(request);

        assertThat(response.getParsedFilters().getCityId()).isEqualTo(330100L);
    }

    @Test
    void shouldExtractCityHintWhenAiDegrades() {
        AiFacade aiFacade = mock(AiFacade.class);
        PostService postService = mock(PostService.class);
        SysRegionMapper regionMapper = mock(SysRegionMapper.class);
        SearchServiceImpl service = new SearchServiceImpl(aiFacade, postService, regionMapper);

        when(aiFacade.parseSearchQuery(any())).thenThrow(new AiDegradedException("offline", null));
        when(regionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(region(330100L, 2, "杭州市"), region(330200L, 2, "宁波市")));
        when(postService.listPublicPosts(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(PostPageVO.<PostListItemVO>builder().page(1).size(8).total(0).records(List.of()).build());

        AiSearchRequest request = new AiSearchRequest();
        request.setQuery("杭州大厨 8000 以上");
        request.setCityId(330200L);
        request.setPage(1);
        request.setSize(8);

        AiSearchResponse response = service.aiSearch(request);

        assertThat(response.getParsedFilters().getCityId()).isEqualTo(330100L);
        assertThat(response.isDegraded()).isTrue();
    }

    @Test
    void shouldRelaxSoftFiltersWhenPreciseSearchIsEmpty() {
        AiFacade aiFacade = mock(AiFacade.class);
        PostService postService = mock(PostService.class);
        SysRegionMapper regionMapper = mock(SysRegionMapper.class);
        SearchServiceImpl service = new SearchServiceImpl(aiFacade, postService, regionMapper);

        SearchParseResponse parsed = new SearchParseResponse();
        parsed.setIntent("search");
        PostSearchFilter filter = new PostSearchFilter();
        filter.setPostType("RECRUIT");
        filter.setJobRole("服务员");
        filter.setShopCategory("火锅");
        parsed.setFilters(filter);
        when(aiFacade.parseSearchQuery(any())).thenReturn(parsed);
        when(postService.listPublicPosts(eq("RECRUIT"), any(), any(), any(), any(), any(), eq("服务员"), eq("火锅"), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(PostPageVO.<PostListItemVO>builder().page(1).size(8).total(0).records(List.of()).build());
        when(postService.listPublicPosts(eq("RECRUIT"), any(), any(), any(), any(), any(), eq("服务员"), eq(null), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(PostPageVO.<PostListItemVO>builder().page(1).size(8).total(1).records(List.of(card("杭州西湖招聘服务员"))).build());

        AiSearchRequest request = new AiSearchRequest();
        request.setQuery("火锅服务员");
        request.setPage(1);
        request.setSize(8);

        AiSearchResponse response = service.aiSearch(request);

        assertThat(response.getCards()).hasSize(1);
        assertThat(response.getParsedFilters().getJobRole()).isEqualTo("服务员");
        assertThat(response.getParsedFilters().getShopCategory()).isNull();
        assertThat(response.getReply()).contains("放宽");
    }

    private SysRegion region(Long id, int level, String name) {
        SysRegion region = new SysRegion();
        region.setId(id);
        region.setLevel(level);
        region.setName(name);
        region.setEnabled(1);
        return region;
    }

    private PostListItemVO card(String title) {
        return PostListItemVO.builder()
                .id("1")
                .postType("RECRUIT")
                .title(title)
                .build();
    }
}
