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
            SearchOutcome outcome = expandIfEmpty(query, list);
            return AiSearchResponse.builder()
                    .reply(replyForOutcome(parsed.getReply(), query, outcome))
                    .degraded(false)
                    .confidence(parsed.getConfidence())
                    .parsedFilters(outcome.query())
                    .list(outcome.list())
                    .cards(topCards(outcome.list()))
                    .messageType(outcome.list().getRecords().isEmpty() ? "TEXT" : "CARDS")
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
        Long cityId = firstNonNull(safe.getCityId(), resolveRegionId(safe.getCityName(), 2), request.getCityId());
        Long districtId = firstNonNull(safe.getDistrictId(), resolveRegionId(safe.getDistrictName(), 3), request.getDistrictId());
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

    private SearchOutcome expandIfEmpty(PostSearchQuery query, PostPageVO<PostListItemVO> list) {
        if (hasRecords(list)) {
            return new SearchOutcome(query, list, false, null);
        }
        PostSearchQuery expanded = null;
        String reason = null;
        if (!isBlank(query.getJobRole()) && !isBlank(query.getShopCategory())) {
            expanded = copyQuery(query).shopCategory(null).build();
            reason = "没有完全匹配的结果，先保留岗位为你放宽了门店类型。";
        } else if (shouldRelaxTypeFilters(query)) {
            expanded = copyQuery(query).jobRole(null).shopCategory(null).canCatering(null).canOpenFlame(null).build();
            reason = "没有完全匹配的结果，先保留地区和信息类型为你扩大范围。";
        } else if (!isBlank(query.getKeyword()) && query.getPostType() != null) {
            expanded = copyQuery(query).keyword(null).build();
            reason = "没有完全匹配关键词的结果，先按信息类型为你扩大范围。";
        }
        if (expanded == null) {
            return new SearchOutcome(query, list, false, null);
        }
        PostPageVO<PostListItemVO> expandedList = search(expanded);
        return hasRecords(expandedList)
                ? new SearchOutcome(expanded, expandedList, true, reason)
                : new SearchOutcome(query, list, false, null);
    }

    private boolean shouldRelaxTypeFilters(PostSearchQuery query) {
        return !isBlank(query.getJobRole())
                || !isBlank(query.getShopCategory())
                || query.getCanCatering() != null
                || query.getCanOpenFlame() != null;
    }

    private boolean hasRecords(PostPageVO<PostListItemVO> list) {
        return list != null && list.getRecords() != null && !list.getRecords().isEmpty();
    }

    private PostSearchQuery.PostSearchQueryBuilder copyQuery(PostSearchQuery query) {
        return PostSearchQuery.builder()
                .keyword(query.getKeyword())
                .postType(query.getPostType())
                .cityId(query.getCityId())
                .districtId(query.getDistrictId())
                .minSalary(query.getMinSalary())
                .maxSalary(query.getMaxSalary())
                .jobRole(query.getJobRole())
                .shopCategory(query.getShopCategory())
                .canCatering(query.getCanCatering())
                .canOpenFlame(query.getCanOpenFlame())
                .sort(query.getSort())
                .page(query.getPage())
                .size(query.getSize());
    }

    private AiSearchResponse degradedSearch(String rawQuery, AiSearchRequest request, int page, int size, String reply, Double confidence) {
        Long cityId = resolveRegionId(extractRegionHint(rawQuery, 2), 2);
        Long districtId = resolveRegionId(extractRegionHint(rawQuery, 3), 3);
        PostSearchQuery query = PostSearchQuery.builder()
                .keyword(rawQuery)
                .cityId(cityId != null ? cityId : request.getCityId())
                .districtId(districtId != null ? districtId : request.getDistrictId())
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

    private String extractRegionHint(String query, int level) {
        String normalized = normalizeText(query);
        if (isBlank(normalized)) {
            return null;
        }
        List<String> candidates = regionNames(level);
        for (String candidate : candidates) {
            if (normalized.contains(candidate)) {
                return candidate;
            }
            String trimmed = candidate.replace("市", "").replace("区", "").replace("县", "");
            if (!trimmed.isBlank() && normalized.contains(trimmed)) {
                return candidate;
            }
        }
        return null;
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

    private String replyForOutcome(String reply, PostSearchQuery query, SearchOutcome outcome) {
        if (outcome.expanded()) {
            return outcome.reason();
        }
        if (!hasRecords(outcome.list())) {
            return "暂时没有找到完全匹配的信息，可以换个说法或放宽条件。";
        }
        return defaultReply(reply, query);
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

    private record SearchOutcome(PostSearchQuery query, PostPageVO<PostListItemVO> list, boolean expanded, String reason) {
    }
}
