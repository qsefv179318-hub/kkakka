package com.kkacca.kkacca.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * YouTube Data API v3 호출 래퍼
 * - searchVideos : 키워드로 영상 ID 목록 검색 (알고리즘 1단계)
 * - getVideoStatistics : 영상 ID로 통계 조회 (알고리즘 2단계)
 * - getCommentsPage : 댓글 1페이지 조회 (알고리즘 3단계, 페이지네이션은 호출자가 nextPageToken으로 관리)
 */
@Slf4j
@Component
public class YoutubeApiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    public YoutubeApiClient(
            @Value("${youtube.api-key}") String apiKey,
            @Value("${youtube.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder().build();
    }

    /** [1단계] search.list 호출 — 키워드 검색 결과(영상 ID) 반환 */
    public YoutubeSearchResponse searchVideos(String keyword, int maxResults) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("q", keyword)
                .queryParam("maxResults", maxResults)
                .queryParam("order", "relevance")
                .queryParam("key", apiKey)
                .build().encode().toUri();

        log.info("[YouTube] search.list keyword='{}' maxResults={}", keyword, maxResults);

        try {
            return webClient.get().uri(uri).retrieve()
                    .bodyToMono(YoutubeSearchResponse.class).block();
        } catch (Exception e) {
            log.error("[YouTube] search.list 실패 keyword={}: {}", keyword, e.getMessage());
            return null;
        }
    }

    /** [2단계] videos.list 호출 — 영상 ID 리스트로 통계(viewCount/likeCount/commentCount) 조회 */
    public YoutubeVideoResponse getVideoStatistics(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) return null;

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/videos")
                .queryParam("part", "snippet,statistics")
                .queryParam("id", String.join(",", videoIds))
                .queryParam("key", apiKey)
                .build().encode().toUri();

        log.info("[YouTube] videos.list ids={}건", videoIds.size());

        try {
            return webClient.get().uri(uri).retrieve()
                    .bodyToMono(YoutubeVideoResponse.class).block();
        } catch (Exception e) {
            log.error("[YouTube] videos.list 실패: {}", e.getMessage());
            return null;
        }
    }

    /** [3단계] commentThreads.list 1페이지 호출 — pageToken=null 이면 첫 페이지 */
    public YoutubeCommentResponse getCommentsPage(String videoId, String pageToken, int maxPerPage) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/commentThreads")
                .queryParam("part", "snippet")
                .queryParam("videoId", videoId)
                .queryParam("maxResults", maxPerPage)
                .queryParam("textFormat", "plainText")
                .queryParam("order", "time")
                .queryParam("key", apiKey);

        if (pageToken != null && !pageToken.isBlank()) {
            builder.queryParam("pageToken", pageToken);
        }

        URI uri = builder.build().encode().toUri();

        try {
            return webClient.get().uri(uri).retrieve()
                    .bodyToMono(YoutubeCommentResponse.class).block();
        } catch (Exception e) {
            // 댓글 비활성화 영상은 403 반환 → null로 처리해 스킵
            log.warn("[YouTube] commentThreads 실패 videoId={}: {}", videoId, e.getMessage());
            return null;
        }
    }
}
