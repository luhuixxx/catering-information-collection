package com.catering.service.search.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.ai.dto.PostSearchFilter;
import com.catering.ai.dto.SearchParseRequest;
import com.catering.ai.dto.SearchParseResponse;
import com.catering.ai.exception.AiDegradedException;
import com.catering.ai.facade.AiFacade;
import com.catering.dao.mapper.SysRegionMapper;
import com.catering.model.entity.SysRegion;
import com.catering.model.enums.PostType;
import com.catering.service.post.PostService;
import com.catering.service.post.dto.PostListItemVO;
import com.catering.service.post.dto.PostPageVO;
import com.catering.service.search.SearchService;
import com.catering.service.search.dto.AiSearchRequest;
import com.catering.service.search.dto.AiSearchResponse;
import com.catering.service.search.dto.PostSearchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final List<String> JOB_TYPES = List.of("大厨", "厨师", "传菜员", "服务员", "收银员", "店长", "洗碗工", "其他");
    private static final List<String> SHOP_CATEGORIES = List.of("中餐", "火锅", "烧烤", "快餐", "茶饮", "其他");

    private final AiFacade aiFacade;
    private final PostService postService;
    private final SysRegionMapper sysRegionMapper;

    @Override
    public AiSearchResponse aiSearch(AiSearchRequest request) {
        String rawQuery = normalizeText(request.getQuery());
        int page = safePage(request.getPage());
        int size = safeSize(request.getSize());
        try {
            SearchParseResponse parsed = aiFacade.parseSearchQuery(buildParseRequest(rawQuery, request.getMessages()));
            if (parsed == null || !"search".equalsIgnoreCase(parsed.getIntent())) {
                return degradedSearch(rawQuery, request, page, size,
                        parsed == null || isBlank(parsed.getReply()) ? "没太听懂，先按关键词帮你找找。" : parsed.getReply(),
                        parsed == null ? null : parsed.getConfidence());
            }
            PostSearchQuery query = toPostSearchQuery(rawQuery, request, parsed.getFilters(), page, size);
            PostPageVO<PostListItemVO> list = search(query);
            return AiSearchResponse.builder()
                    .reply(defaultReply(parsed.getReply(), query))
                    .degraded(false)
                    .confidence(parsed.getConfidence())
                    .parsedFilters(query)
                    .list(list)
                    .cards(topCards(list))
                    .messageType(list.getRecords().isEmpty() ? "TEXT" : "CARDS")
                    .build();
        } catch (AiDegradedException ex) {
            return degradedSearch(rawQuery, request, page, size, "AI 暂时不可用，已按关键词为你搜索。", null);
        }
    }

    private SearchParseRequest buildParseRequest(String query, List<AiSearchRequest.Message> messages) {
        return SearchParseRequest.builder()
                .query(query)
                .messages(toParseMessages(messages))
                .context(SearchParseRequest.SearchContext.builder()
                        .province("浙江省")
                        .postTypes(Arrays.stream(PostType.values()).map(Enum::name).toList())
                        .cities(regionNames(2))
                        .districts(regionNames(3))
                        .jobTypes(JOB_TYPES)
                        .shopCategories(SHOP_CATEGORIES)
                        .build())
                .build();
    }

    private List<SearchParseRequest.Message> toParseMessages(List<AiSearchRequest.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        return messages.stream()
                .filter(message -> message != null && !isBlank(message.getContent()))
                .skip(Math.max(0, messages.size() - 8))
                .map(message -> SearchParseRequest.Message.builder()
                        .role(normalizeText(message.getRole()))
                        .content(normalizeText(message.getContent()))
                        .build())
                .toList();
    }

    private PostSearchQuery toPostSearchQuery(String rawQuery, AiSearchRequest request, PostSearchFilter filter, int page, int size) {
        PostSearchFilter safe = filter == null ? new PostSearchFilter() : filter;
        Long cityId = firstNonNull(safe.getCityId(), request.getCityId(), resolveRegionId(safe.getCityName(), 2));
        Long districtId = firstNonNull(safe.getDistrictId(), request.getDistrictId(), resolveRegionId(safe.getDistrictName(), 3));
        String keyword = firstText(safe.getKeyword(), hasStructuredFilters(safe) ? null : rawQuery);
        return PostSearchQuery.builder()
                .keyword(keyword)
                .postType(normalizePostType(safe.getPostType()))
                .cityId(cityId)
                .districtId(districtId)
                .minSalary(safe.getMinSalary())
                .maxSalary(safe.getMaxSalary())
                .jobRole(normalizeText(safe.getJobRole()))
                .shopCategory(normalizeText(safe.getShopCategory()))
                .canCatering(safe.getCanCatering())
                .canOpenFlame(safe.getCanOpenFlame())
                .sort(firstText(safe.getSort(), "DEFAULT").toUpperCase(Locale.ROOT))
                .page(page)
                .size(size)
                .build();
    }

    private PostPageVO<PostListItemVO> search(PostSearchQuery query) {
        return postService.listPublicPosts(query.getPostType(), query.getCityId(), query.getDistrictId(),
                query.getKeyword(), query.getMinSalary(), query.getMaxSalary(), query.getJobRole(),
                query.getShopCategory(), query.getCanCatering(), query.getCanOpenFlame(),
                query.getSort(), query.getPage(), query.getSize());
    }

    private AiSearchResponse degradedSearch(String rawQuery, AiSearchRequest request, int page, int size, String reply, Double confidence) {
        PostSearchQuery query = PostSearchQuery.builder()
                .keyword(rawQuery)
                .cityId(request.getCityId())
                .districtId(request.getDistrictId())
                .sort("DEFAULT")
                .page(page)
                .size(size)
                .build();
        PostPageVO<PostListItemVO> list = search(query);
        return AiSearchResponse.builder()
                .reply(reply)
                .degraded(true)
                .confidence(confidence)
                .parsedFilters(query)
                .list(list)
                .cards(topCards(list))
                .messageType(list.getRecords().isEmpty() ? "TEXT" : "CARDS")
                .build();
    }

    private List<PostListItemVO> topCards(PostPageVO<PostListItemVO> list) {
        if (list == null || list.getRecords() == null) {
            return List.of();
        }
        return list.getRecords().stream().limit(5).toList();
    }

    private List<String> regionNames(int level) {
        return sysRegionMapper.selectList(new LambdaQueryWrapper<SysRegion>()
                        .eq(SysRegion::getEnabled, 1)
                        .eq(SysRegion::getLevel, level)
                        .orderByAsc(SysRegion::getSortNo)
                        .orderByAsc(SysRegion::getId))
                .stream()
                .map(SysRegion::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
    }

    private Long resolveRegionId(String name, int level) {
        String normalized = normalizeText(name);
        if (isBlank(normalized)) {
            return null;
        }
        return sysRegionMapper.selectList(new LambdaQueryWrapper<SysRegion>()
                .eq(SysRegion::getEnabled, 1)
                .eq(SysRegion::getLevel, level)
                .orderByAsc(SysRegion::getSortNo)
                .orderByAsc(SysRegion::getId))
                .stream()
                .filter(region -> {
                    String regionName = normalizeText(region.getName());
                    return regionName.equals(normalized) || regionName.contains(normalized) || normalized.contains(regionName);
                })
                .map(SysRegion::getId)
                .findFirst()
                .orElse(null);
    }

    private String normalizePostType(String value) {
        String text = normalizeText(value);
        if (isBlank(text)) {
            return null;
        }
        try {
            return PostType.valueOf(text.toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String defaultReply(String reply, PostSearchQuery query) {
        if (!isBlank(reply)) {
            return reply;
        }
        return query.getPostType() == null ? "已按你的描述筛选信息。" : "已按你的描述筛选" + query.getPostType() + "信息。";
    }

    private boolean hasStructuredFilters(PostSearchFilter filter) {
        return !isBlank(filter.getPostType())
                || filter.getCityId() != null
                || filter.getDistrictId() != null
                || !isBlank(filter.getCityName())
                || !isBlank(filter.getDistrictName())
                || filter.getMinSalary() != null
                || filter.getMaxSalary() != null
                || !isBlank(filter.getJobRole())
                || !isBlank(filter.getShopCategory())
                || filter.getCanCatering() != null
                || filter.getCanOpenFlame() != null;
    }

    private int safePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int safeSize(Integer size) {
        if (size == null) {
            return 20;
        }
        return Math.min(Math.max(size, 1), 100);
    }

    private <T> T firstNonNull(T first, T second, T third) {
        if (first != null) {
            return first;
        }
        return second != null ? second : third;
    }

    private String firstText(String first, String second) {
        String normalized = normalizeText(first);
        return isBlank(normalized) ? normalizeText(second) : normalized;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
