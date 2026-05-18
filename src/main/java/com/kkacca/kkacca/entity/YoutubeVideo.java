package com.kkacca.kkacca.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB 컬렉션: youtube_videos
 * 알고리즘 2단계 — 영상 ID + 통계를 저장하며 commentsCollected 플래그로 댓글 수집 진행 상태를 추적
 */
@Document(collection = "youtube_videos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class YoutubeVideo {

    @Id
    private String videoId;          // YouTube videoId (PK)

    @Indexed
    private Long snackId;            // 어떤 과자 키워드로 검색되어 수집됐는지

    private String snackKeyword;     // 검색 키워드(과자 이름)
    private String title;
    private String channelTitle;
    private LocalDateTime publishedAt;

    private Long viewCount;
    private Long likeCount;
    private Long commentCount;

    @Indexed
    private Boolean commentsCollected; // 알고리즘 3단계 트리거 플래그

    private LocalDateTime collectedAt;
}
