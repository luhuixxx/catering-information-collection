package com.catering.api;

import com.catering.dao.mapper.PostMapper;
import com.catering.service.post.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FoundationSmokeTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostService postService;

    @Autowired
    private PostMapper postMapper;

    @Test
    void shouldExposeCommonHealthAndOpenApi() {
        ResponseEntity<String> health = restTemplate.getForEntity(url("/api/common/health"), String.class);
        ResponseEntity<String> apiDocs = restTemplate.getForEntity(url("/v3/api-docs"), String.class);
        ResponseEntity<String> appDocs = restTemplate.getForEntity(url("/v3/api-docs/app"), String.class);

        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(apiDocs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(appDocs.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldProtectAppAndAdminEndpointsWithoutToken() {
        ResponseEntity<String> appPing = restTemplate.getForEntity(url("/api/app/ping"), String.class);
        ResponseEntity<String> adminPing = restTemplate.getForEntity(url("/api/admin/ping"), String.class);

        assertThat(appPing.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(adminPing.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowAnonymousAiSearchWithDegradedFallback() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"query\":\"杭州大厨 8000 以上\",\"page\":1,\"size\":5}";

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/app/search/ai"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"code\":0");
        assertThat(response.getBody()).contains("\"degraded\":true");
    }

    @Test
    void shouldLoadMybatisMapperAndServiceBeans() {
        assertThat(postService).isNotNull();
        assertThat(postMapper).isNotNull();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
