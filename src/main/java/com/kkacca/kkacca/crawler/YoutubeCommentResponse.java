package com.kkacca.kkacca.crawler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * YouTube Data API v3 - commentThreads.list 응답 매핑
 * 영상의 최상위 댓글을 페이지네이션으로 수집할 때 사용
 */
@Getter @Setter @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeCommentResponse {

    private String nextPageToken;
    private List<Item> items;

    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String id;             // commentThread ID
        private Snippet snippet;
    }

    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snippet {
        private TopLevelComment topLevelComment;
    }

    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopLevelComment {
        private String id;             // comment ID
        private CommentSnippet snippet;
    }

    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentSnippet {
        private String textOriginal;       // 댓글 본문
        private String authorDisplayName;
        private Integer likeCount;
        private String publishedAt;
    }
}
