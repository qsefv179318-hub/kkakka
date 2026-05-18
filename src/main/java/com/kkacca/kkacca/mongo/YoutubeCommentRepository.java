package com.kkacca.kkacca.mongo;

import com.kkacca.kkacca.entity.YoutubeComment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * youtube_comments 컬렉션 접근
 * - findBySnackId: 알고리즘 4단계 AI 분석 입력용 (과자별 전체 댓글)
 */
public interface YoutubeCommentRepository extends MongoRepository<YoutubeComment, String> {

    List<YoutubeComment> findBySnackId(Long snackId);
}
