package com.kkacca.kkacca.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
public class NaverApiClient {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String shopSearchUrl;

    public NaverApiClient(
            @Value("${naver.client-id}") String clientId,
            @Value("${naver.client-secret}") String clientSecret,
            @Value("${naver.shop-search-url}") String shopSearchUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.shopSearchUrl = shopSearchUrl;
        this.webClient = WebClient.builder().build();
    }

    /**
     * 네이버 쇼핑 검색 호출
     * @param keyword 검색어 (과자명)
     * @param display 결과 개수 (최대 100)
     */
    public NaverShopReviewItem searchShop(String keyword, int display) {
        URI uri = UriComponentsBuilder.fromHttpUrl(shopSearchUrl)
                .queryParam("query", keyword)
                .queryParam("display", display)
                .queryParam("sort", "sim")
                .build()
                .encode()
                .toUri();

        log.info("[NaverAPI] 호출: {}", uri);

        try {
            return webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .bodyToMono(NaverShopReviewItem.class)
                    .block();
        } catch (Exception e) {
            log.error("[NaverAPI] 호출 실패 keyword={}: {}", keyword, e.getMessage());
            return null;
        }
    }
}
