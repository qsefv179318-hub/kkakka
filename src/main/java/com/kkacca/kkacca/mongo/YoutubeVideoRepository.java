package com.kkacca.kkacca.mongo;

import com.kkacca.kkacca.entity.YoutubeVideo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * youtube_videos 컬렉션 접근
 * - existsById: 알고리즘 1단계 중복 영상 ID 체크
 * - findByCommentsCollectedFalse: 알고리즘 3단계 댓글 미수집 영상 조회
 */
public interface YoutubeVideoRepository extends MongoRepository<YoutubeVideo, String> {

    List<YoutubeVideo> findByCommentsCollectedFalse();

    List<YoutubeVideo> findBySnackId(Long snackId);
}
