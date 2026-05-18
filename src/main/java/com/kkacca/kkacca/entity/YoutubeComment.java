package com.kkacca.kkacca.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB 컬렉션: youtube_comments
 * 알고리즘 3단계 — commentThreads.list 페이지네이션으로 수집된 영상별 댓글 저장
 * snackId 인덱스: AI 분석 단계에서 과자별 댓글 조회를 위함
 */
@Document(collection = "youtube_comments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class YoutubeComment {

    @Id
    private String commentId;        // YouTube comment ID (PK)

    @Indexed
    private String videoId;

    @Indexed
    private Long snackId;

    private String content;
    private String authorName;
    private Integer likeCount;
    private LocalDateTime publishedAt;

    private LocalDateTime collectedAt;
}
