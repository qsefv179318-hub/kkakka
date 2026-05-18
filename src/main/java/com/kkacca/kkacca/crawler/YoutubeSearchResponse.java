package com.kkacca.kkacca.crawler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * YouTube Data API v3 - search.list 응답 매핑
 * 영상 ID 목록을 얻기 위한 1차 호출 결과
 */
@Getter @Setter @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeSearchResponse {

    private String nextPageToken;
    private List<Item> items;

    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Id id;
        private Snippet snippet;
    }

    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Id {
        private String kind;     // "youtube#video"
        private String videoId;
    }

    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snippet {
        private String publishedAt;
        private String channelId;
        private String title;
        private String description;
        private String channelTitle;
    }
}
