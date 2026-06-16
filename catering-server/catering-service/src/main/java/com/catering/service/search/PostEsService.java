package com.catering.service.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.dao.mapper.PostFranchiseMapper;
import com.catering.dao.mapper.PostJobSeekMapper;
import com.catering.dao.mapper.PostMapper;
import com.catering.dao.mapper.PostRentMapper;
import com.catering.dao.mapper.PostRecruitMapper;
import com.catering.dao.mapper.PostTransferMapper;
import com.catering.model.entity.Post;
import com.catering.model.entity.PostFranchise;
import com.catering.model.entity.PostJobSeek;
import com.catering.model.entity.PostRent;
import com.catering.model.entity.PostRecruit;
import com.catering.model.entity.PostTransfer;
import com.catering.model.enums.PostStatus;
import com.catering.model.enums.PostType;
import com.catering.service.search.config.EsProperties;
import com.catering.service.search.dto.PostSearchQuery;
import com.catering.service.search.dto.PostSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostEsService {

    private final EsProperties esProperties;
    private final PostMapper postMapper;
    private final PostRecruitMapper postRecruitMapper;
    private final PostTransferMapper postTransferMapper;
    private final PostRentMapper postRentMapper;
    private final PostJobSeekMapper postJobSeekMapper;
    private final PostFranchiseMapper postFranchiseMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostConstruct
    public void warmupSync() {
        try {
            if (isAvailable()) {
                int count = rebuildIndex();
                log.info("ES warmup sync completed. count={}", count);
            }
        } catch (Exception e) {
            log.warn("ES warmup skipped. message={}", e.getMessage());
        }
    }

    public Optional<PostSearchResult> search(PostSearchQuery query) {
        try {
            int page = Math.max(query.getPage(), 1);
            int size = Math.min(Math.max(query.getSize(), 1), 100);
            int from = (page - 1) * size;

            Map<String, Object> root = new HashMap<>();
            root.put("from", from);
            root.put("size", size);
            root.put("_source", false);
            root.put("track_total_hits", true);
            root.put("sort", buildSort(query.getSort()));

            List<Object> filters = baseFilters(query);
            appendTypeFilters(filters, query);

            Map<String, Object> bool = new LinkedHashMap<>();
            bool.put("filter", filters);
            if (hasKeyword(query.getKeyword())) {
                bool.put("must", List.of(Map.of("multi_match", Map.of(
                        "query", query.getKeyword().trim(),
                        "type", "best_fields",
                        "fields", searchFields()
                ))));
            } else {
                bool.put("must", List.of(Map.of("match_all", Map.of())));
            }
            root.put("query", Map.of("bool", bool));

            JsonNode body = post("/" + esProperties.getIndexAlias() + "/_search", objectMapper.writeValueAsString(root));
            List<Long> ids = new ArrayList<>();
            JsonNode hits = body.path("hits").path("hits");
            for (JsonNode hit : hits) {
                String idText = hit.path("_id").asText("");
                if (!idText.isBlank()) {
                    ids.add(Long.parseLong(idText));
                }
            }
            long total = body.path("hits").path("total").path("value").asLong(ids.size());
            return Optional.of(PostSearchResult.builder().ids(ids).total(total).build());
        } catch (Exception e) {
            log.warn("ES search failed, fallback to DB. message={}", e.getMessage());
            return Optional.empty();
        }
    }

    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("baseUrl", esProperties.getBaseUrl());
        result.put("indexAlias", esProperties.getIndexAlias());
        try {
            JsonNode cluster = get("/_cluster/health");
            result.put("clusterStatus", cluster.path("status").asText("unknown"));
            result.put("available", true);
        } catch (Exception e) {
            result.put("available", false);
            result.put("clusterStatus", "unreachable");
            result.put("message", e.getMessage());
            return result;
        }
        try {
            JsonNode alias = get("/_alias/" + esProperties.getIndexAlias());
            result.put("indexExists", !alias.isEmpty());
            if (!alias.isEmpty()) {
                String indexName = alias.fieldNames().next();
                JsonNode count = get("/" + indexName + "/_count");
                result.put("documentCount", count.path("count").asLong(0));
            } else {
                result.put("documentCount", 0);
            }
        } catch (Exception e) {
            result.put("indexExists", false);
            result.put("documentCount", 0);
            result.put("indexMessage", e.getMessage());
        }
        return result;
    }

    public int rebuildIndex() {
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, PostStatus.APPROVED)
                .gt(Post::getExpireAt, LocalDateTime.now()));
        int synced = 0;
        for (Post post : posts) {
            try {
                syncPost(post.getId());
                synced++;
            } catch (Exception ignored) {
                // logged in syncPost
            }
        }
        return synced;
    }

    public boolean isAvailable() {
        try {
            get("/_cluster/health");
            get("/_alias/" + esProperties.getIndexAlias());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void syncPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != PostStatus.APPROVED || post.getExpireAt() == null || !post.getExpireAt().isAfter(LocalDateTime.now())) {
            removePost(postId);
            return;
        }
        try {
            Map<String, Object> doc = buildDoc(post);
            put("/" + esProperties.getIndexAlias() + "/_doc/" + postId, objectMapper.writeValueAsString(doc));
        } catch (Exception e) {
            log.warn("ES sync post failed. postId={}, message={}", postId, e.getMessage());
        }
    }

    public void removePost(Long postId) {
        try {
            delete("/" + esProperties.getIndexAlias() + "/_doc/" + postId);
        } catch (Exception e) {
            log.warn("ES remove post failed. postId={}, message={}", postId, e.getMessage());
        }
    }

    private List<Object> baseFilters(PostSearchQuery query) {
        List<Object> filters = new ArrayList<>();
        filters.add(Map.of("term", Map.of("status", "APPROVED")));
        filters.add(Map.of("range", Map.of("expireAt", Map.of("gt", "now"))));
        if (query.getPostType() != null && !query.getPostType().isBlank()) {
            filters.add(Map.of("term", Map.of("postType", query.getPostType())));
        }
        if (query.getCityId() != null) {
            filters.add(Map.of("term", Map.of("cityId", query.getCityId())));
        }
        if (query.getDistrictId() != null) {
            filters.add(Map.of("term", Map.of("districtId", query.getDistrictId())));
        }
        return filters;
    }

    private void appendTypeFilters(List<Object> filters, PostSearchQuery query) {
        if (query.getPostType() == null || query.getPostType().isBlank()) {
            return;
        }
        PostType type = PostType.valueOf(query.getPostType());
        if (type == PostType.RECRUIT) {
            appendTermFilter(filters, "recruit.jobRole", query.getJobRole());
            appendTermFilter(filters, "recruit.shopCategory", query.getShopCategory());
            appendSalaryOverlap(filters, "recruit.salaryMin", "recruit.salaryMax", query.getMinSalary(), query.getMaxSalary());
        } else if (type == PostType.TRANSFER) {
            appendTermFilter(filters, "transfer.shopCategory", query.getShopCategory());
        } else if (type == PostType.RENT) {
            if (query.getMinSalary() != null) {
                filters.add(Map.of("range", Map.of("rent.rentMonthly", Map.of("gte", query.getMinSalary()))));
            }
            if (query.getMaxSalary() != null) {
                filters.add(Map.of("range", Map.of("rent.rentMonthly", Map.of("lte", query.getMaxSalary()))));
            }
            appendBoolFilter(filters, "rent.canCatering", query.getCanCatering());
            appendBoolFilter(filters, "rent.canOpenFlame", query.getCanOpenFlame());
        } else if (type == PostType.JOB_SEEK) {
            appendWildcardFilter(filters, "jobSeek.desiredRoles", query.getJobRole());
            appendWildcardFilter(filters, "jobSeek.cuisines", query.getShopCategory());
            appendSalaryOverlap(filters, "jobSeek.salaryMin", "jobSeek.salaryMax", query.getMinSalary(), query.getMaxSalary());
        } else if (type == PostType.FRANCHISE) {
            appendWildcardFilter(filters, "franchise.brandName", query.getJobRole());
            appendWildcardFilter(filters, "franchise.category", query.getShopCategory());
        }
    }

    private void appendTermFilter(List<Object> filters, String field, String value) {
        if (value != null && !value.isBlank()) {
            filters.add(Map.of("term", Map.of(field, value)));
        }
    }

    private void appendWildcardFilter(List<Object> filters, String field, String value) {
        if (value != null && !value.isBlank()) {
            filters.add(Map.of("wildcard", Map.of(field, Map.of("value", "*" + value.trim() + "*"))));
        }
    }

    private void appendBoolFilter(List<Object> filters, String field, Boolean value) {
        if (value != null) {
            filters.add(Map.of("term", Map.of(field, value)));
        }
    }

    private void appendSalaryOverlap(List<Object> filters, String minField, String maxField, Integer minSalary, Integer maxSalary) {
        if (minSalary != null) {
            List<Object> should = new ArrayList<>();
            should.add(Map.of("range", Map.of(maxField, Map.of("gte", minSalary))));
            should.add(Map.of("bool", Map.of("must_not", Map.of("exists", Map.of("field", maxField)))));
            filters.add(Map.of("bool", Map.of("should", should, "minimum_should_match", 1)));
        }
        if (maxSalary != null) {
            List<Object> should = new ArrayList<>();
            should.add(Map.of("range", Map.of(minField, Map.of("lte", maxSalary))));
            should.add(Map.of("bool", Map.of("must_not", Map.of("exists", Map.of("field", minField)))));
            filters.add(Map.of("bool", Map.of("should", should, "minimum_should_match", 1)));
        }
    }

    private List<Object> buildSort(String sort) {
        List<Object> sortList = new ArrayList<>();
        if ("LATEST".equalsIgnoreCase(sort)) {
            sortList.add(Map.of("createdAt", Map.of("order", "desc")));
            return sortList;
        }
        if ("EXPIRE_SOON".equalsIgnoreCase(sort)) {
            sortList.add(Map.of("expireAt", Map.of("order", "asc")));
            sortList.add(Map.of("createdAt", Map.of("order", "desc")));
            return sortList;
        }
        sortList.add(Map.of("isTop", Map.of("order", "desc")));
        sortList.add(Map.of("topUntil", Map.of("order", "desc", "missing", "_last")));
        sortList.add(Map.of("createdAt", Map.of("order", "desc")));
        return sortList;
    }

    private List<String> searchFields() {
        return List.of(
                "title^4",
                "description^2",
                "address^2",
                "postNo^3",
                "recruit.jobRole^2",
                "recruit.shopCategory",
                "recruit.cuisines",
                "transfer.shopCategory",
                "jobSeek.desiredRoles^2",
                "jobSeek.cuisines",
                "franchise.brandName^3",
                "franchise.category",
                "franchise.investmentDesc",
                "franchise.franchiseDesc"
        );
    }

    private Map<String, Object> buildDoc(Post post) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", post.getId());
        doc.put("postNo", nvl(post.getPostNo()));
        doc.put("postType", post.getPostType().name());
        doc.put("status", post.getStatus().name());
        putIfNotNull(doc, "provinceId", post.getProvinceId());
        putIfNotNull(doc, "cityId", post.getCityId());
        putIfNotNull(doc, "districtId", post.getDistrictId());
        doc.put("title", nvl(post.getTitle()));
        doc.put("description", nvl(post.getDescription()));
        doc.put("address", nvl(post.getAddress()));
        doc.put("isTop", post.getIsTop() != null && post.getIsTop() == 1);
        putIfNotNull(doc, "topUntil", post.getTopUntil());
        putIfNotNull(doc, "expireAt", post.getExpireAt());
        putIfNotNull(doc, "createdAt", post.getCreatedAt());
        putIfNotNull(doc, "updatedAt", post.getUpdatedAt());
        doc.put("coverImage", nvl(post.getCoverImage()));

        if (post.getPostType() == PostType.RECRUIT) {
            PostRecruit ext = postRecruitMapper.selectById(post.getId());
            if (ext != null) {
                doc.put("recruit", extMap(
                        "jobRole", nvl(ext.getJobRole()),
                        "shopCategory", nvl(ext.getShopCategory()),
                        "salaryType", ext.getSalaryType() == null ? "" : ext.getSalaryType().name(),
                        "salaryMin", ext.getSalaryMin(),
                        "salaryMax", ext.getSalaryMax(),
                        "provideBoard", ext.getProvideBoard() != null && ext.getProvideBoard() == 1,
                        "expRequirement", nvl(ext.getExpRequirement()),
                        "cuisines", nvl(ext.getCuisines())
                ));
            }
        } else if (post.getPostType() == PostType.TRANSFER) {
            PostTransfer ext = postTransferMapper.selectById(post.getId());
            if (ext != null) {
                doc.put("transfer", extMap(
                        "shopCategory", nvl(ext.getShopCategory()),
                        "areaSqm", ext.getAreaSqm(),
                        "rentMonthly", ext.getRentMonthly(),
                        "rentNegotiable", ext.getRentNegotiable() != null && ext.getRentNegotiable() == 1,
                        "transferFee", ext.getTransferFee(),
                        "feeNegotiable", ext.getFeeNegotiable() != null && ext.getFeeNegotiable() == 1,
                        "includeEquipment", ext.getIncludeEquipment() != null && ext.getIncludeEquipment() == 1,
                        "operating", ext.getOperating() != null && ext.getOperating() == 1
                ));
            }
        } else if (post.getPostType() == PostType.RENT) {
            PostRent ext = postRentMapper.selectById(post.getId());
            if (ext != null) {
                doc.put("rent", extMap(
                        "areaSqm", ext.getAreaSqm(),
                        "rentMonthly", ext.getRentMonthly(),
                        "rentNegotiable", ext.getRentNegotiable() != null && ext.getRentNegotiable() == 1,
                        "entryFee", ext.getEntryFee(),
                        "entryFeeNegotiable", ext.getEntryFeeNegotiable() != null && ext.getEntryFeeNegotiable() == 1,
                        "canCatering", ext.getCanCatering() != null && ext.getCanCatering() == 1,
                        "canOpenFlame", ext.getCanOpenFlame() != null && ext.getCanOpenFlame() == 1,
                        "publisherIdentity", ext.getPublisherIdentity() == null ? "" : ext.getPublisherIdentity().name()
                ));
            }
        } else if (post.getPostType() == PostType.JOB_SEEK) {
            PostJobSeek ext = postJobSeekMapper.selectById(post.getId());
            if (ext != null) {
                doc.put("jobSeek", extMap(
                        "desiredRoles", nvl(ext.getDesiredRoles()),
                        "desiredCities", nvl(ext.getDesiredCities()),
                        "desiredDistricts", nvl(ext.getDesiredDistricts()),
                        "workYears", ext.getWorkYears(),
                        "cuisines", nvl(ext.getCuisines()),
                        "salaryMin", ext.getSalaryMin(),
                        "salaryMax", ext.getSalaryMax()
                ));
            }
        } else if (post.getPostType() == PostType.FRANCHISE) {
            PostFranchise ext = postFranchiseMapper.selectById(post.getId());
            if (ext != null) {
                doc.put("franchise", extMap(
                        "brandName", nvl(ext.getBrandName()),
                        "category", nvl(ext.getCategory()),
                        "investmentDesc", nvl(ext.getInvestmentDesc()),
                        "franchiseDesc", nvl(ext.getFranchiseDesc())
                ));
            }
        }
        return doc;
    }

    private Map<String, Object> extMap(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            Object value = kv[i + 1];
            if (value != null) {
                map.put((String) kv[i], value);
            }
        }
        return map;
    }

    private void putIfNotNull(Map<String, Object> doc, String key, Object value) {
        if (value != null) {
            doc.put(key, value);
        }
    }

    private boolean hasKeyword(String keyword) {
        return keyword != null && !keyword.isBlank();
    }

    private JsonNode get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(normalizeBaseUrl() + path))
                .GET()
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new IllegalStateException("http " + res.statusCode() + ": " + res.body());
        }
        return objectMapper.readTree(res.body());
    }

    private JsonNode post(String path, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(normalizeBaseUrl() + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new IllegalStateException("http " + res.statusCode() + ": " + res.body());
        }
        return objectMapper.readTree(res.body());
    }

    private void put(String path, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(normalizeBaseUrl() + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new IllegalStateException("http " + res.statusCode() + ": " + res.body());
        }
    }

    private void delete(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(normalizeBaseUrl() + path))
                .DELETE()
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 404 && res.statusCode() >= 300) {
            throw new IllegalStateException("http " + res.statusCode() + ": " + res.body());
        }
    }

    private String normalizeBaseUrl() {
        String baseUrl = esProperties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
