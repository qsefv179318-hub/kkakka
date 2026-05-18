package com.kkacca.kkacca.crawler;

import com.kkacca.kkacca.entity.Snack;
import com.kkacca.kkacca.entity.SnackAnalysis;
import com.kkacca.kkacca.entity.YoutubeComment;
import com.kkacca.kkacca.entity.YoutubeVideo;
import com.kkacca.kkacca.mongo.YoutubeCommentRepository;
import com.kkacca.kkacca.mongo.YoutubeVideoRepository;
import com.kkacca.kkacca.repository.SnackAnalysisRepository;
import com.kkacca.kkacca.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * YouTube 댓글 기반 과자 분석 파이프라인 스케줄러
 * 매일 새벽 3시 실행 — 사용자가 요청한 5단계 알고리즘 그대로 구현
 *   1. 과자별 search.list → 영상 ID (중복 skip)
 *   2. videos.list → 통계 함께 MongoDB 저장 (commentsCollected=false)
 *   3. commentsCollected=false 영상 → 댓글 페이지네이션 수집 → true 업데이트
 *   4. 댓글 batchSize개씩 → AI 분석 → 평균 합산 → PostgreSQL snack_analysis 저장
 *   (5단계 React 시각화는 백엔드 범위 밖)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeCrawlerScheduler {

    private final SnackRepository snackRepository;
    private final SnackAnalysisRepository snackAnalysisRepository;
    private final YoutubeVideoRepository videoRepository;
    private final YoutubeCommentRepository commentRepository;
    private final YoutubeApiClient youtubeApiClient;
    private final AiAnalysisClient aiAnalysisClient;

    @Value("${youtube.search-max-results}")
    private int searchMaxResults;

    @Value("${youtube.comment-max-per-page}")
    private int commentMaxPerPage;

    @Value("${ai.batch-size}")
    private int aiBatchSize;

    /** 매일 새벽 3시 자동 실행 */
    @Scheduled(cron = "0 0 3 * * *")
    public void runPipeline() {
        log.info("===== [YouTube 파이프라인] 시작 =====");
        long startMs = System.currentTimeMillis();

        List<Snack> snacks = snackRepository.findAll();

        // 1단계 + 2단계: 영상 검색 및 저장
        for (Snack snack : snacks) {
            try {
                step1And2_searchAndSaveVideos(snack);
                Thread.sleep(300);
            } catch (Exception e) {
                log.error("[1-2단계] snack={} 실패: {}", snack.getName(), e.getMessage());
            }
        }

        // 3단계: 댓글 미수집 영상 → 댓글 수집
        try {
            step3_collectComments();
        } catch (Exception e) {
            log.error("[3단계] 댓글 수집 실패: {}", e.getMessage());
        }

        // 4단계: 과자별 댓글 → AI 분석 → PostgreSQL 저장
        for (Snack snack : snacks) {
            try {
                step4_analyzeAndSave(snack);
            } catch (Exception e) {
                log.error("[4단계] snack={} 분석 실패: {}", snack.getName(), e.getMessage());
            }
        }

        long elapsedSec = (System.currentTimeMillis() - startMs) / 1000;
        log.info("===== [YouTube 파이프라인] 완료: {}초 =====", elapsedSec);
    }

    // =============================================================
    // 1+2단계
    // =============================================================
    private void step1And2_searchAndSaveVideos(Snack snack) {
        YoutubeSearchResponse search = youtubeApiClient.searchVideos(snack.getName(), searchMaxResults);
        if (search == null || search.getItems() == null || search.getItems().isEmpty()) {
            log.warn("[1단계] '{}' 검색 결과 없음", snack.getName());
            return;
        }

        // 중복 영상 ID 제거 (이미 저장된 것은 skip)
        List<String> newVideoIds = search.getItems().stream()
                .map(i -> i.getId() != null ? i.getId().getVideoId() : null)
                .filter(id -> id != null && !videoRepository.existsById(id))
                .distinct()
                .toList();

        if (newVideoIds.isEmpty()) {
            log.info("[1단계] '{}' 신규 영상 0건 (전체 중복)", snack.getName());
            return;
        }

        // 영상 통계 조회 후 저장
        YoutubeVideoResponse stats = youtubeApiClient.getVideoStatistics(newVideoIds);
        if (stats == null || stats.getItems() == null) return;

        List<YoutubeVideo> toSave = new ArrayList<>();
        for (YoutubeVideoResponse.Item item : stats.getItems()) {
            toSave.add(YoutubeVideo.builder()
                    .videoId(item.getId())
                    .snackId(snack.getId())
                    .snackKeyword(snack.getName())
                    .title(item.getSnippet() != null ? item.getSnippet().getTitle() : null)
                    .channelTitle(item.getSnippet() != null ? item.getSnippet().getChannelTitle() : null)
                    .publishedAt(parseIsoDate(item.getSnippet() != null ? item.getSnippet().getPublishedAt() : null))
                    .viewCount(parseLong(item.getStatistics() != null ? item.getStatistics().getViewCount() : null))
                    .likeCount(parseLong(item.getStatistics() != null ? item.getStatistics().getLikeCount() : null))
                    .commentCount(parseLong(item.getStatistics() != null ? item.getStatistics().getCommentCount() : null))
                    .commentsCollected(false)   // 3단계 트리거 플래그
                    .collectedAt(LocalDateTime.now())
                    .build());
        }
        videoRepository.saveAll(toSave);
        log.info("[1-2단계] '{}' 신규 영상 {}건 저장", snack.getName(), toSave.size());
    }

    // =============================================================
    // 3단계: commentsCollected=false 영상의 댓글 전체 수집
    // =============================================================
    private void step3_collectComments() {
        List<YoutubeVideo> targets = videoRepository.findByCommentsCollectedFalse();
        log.info("[3단계] 댓글 수집 대상 영상 {}건", targets.size());

        for (YoutubeVideo video : targets) {
            int collected = 0;
            String pageToken = null;

            do {
                YoutubeCommentResponse page = youtubeApiClient.getCommentsPage(
                        video.getVideoId(), pageToken, commentMaxPerPage);
                if (page == null || page.getItems() == null) break;

                List<YoutubeComment> toSave = new ArrayList<>();
                for (YoutubeCommentResponse.Item item : page.getItems()) {
                    YoutubeCommentResponse.TopLevelComment tlc =
                            item.getSnippet() != null ? item.getSnippet().getTopLevelComment() : null;
                    if (tlc == null || tlc.getSnippet() == null) continue;

                    YoutubeCommentResponse.CommentSnippet cs = tlc.getSnippet();
                    toSave.add(YoutubeComment.builder()
                            .commentId(tlc.getId())
                            .videoId(video.getVideoId())
                            .snackId(video.getSnackId())
                            .content(cs.getTextOriginal())
                            .authorName(cs.getAuthorDisplayName())
                            .likeCount(cs.getLikeCount())
                            .publishedAt(parseIsoDate(cs.getPublishedAt()))
                            .collectedAt(LocalDateTime.now())
                            .build());
                }
                if (!toSave.isEmpty()) {
                    commentRepository.saveAll(toSave);
                    collected += toSave.size();
                }

                pageToken = page.getNextPageToken();
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            } while (pageToken != null && !pageToken.isBlank());

            // 수집 완료 플래그 갱신
            video.setCommentsCollected(true);
            videoRepository.save(video);
            log.info("[3단계] videoId={} 댓글 {}건 수집 완료", video.getVideoId(), collected);
        }
    }

    // =============================================================
    // 4단계: 과자별 댓글 → batchSize씩 AI 분석 → 평균 합산 → PostgreSQL 저장
    // =============================================================
    private void step4_analyzeAndSave(Snack snack) {
        List<YoutubeComment> all = commentRepository.findBySnackId(snack.getId());
        if (all.isEmpty()) {
            log.info("[4단계] '{}' 분석할 댓글 없음", snack.getName());
            return;
        }

        List<String> texts = all.stream()
                .map(YoutubeComment::getContent)
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.toList());

        // batchSize 단위 분할
        List<AiAnalysisResult> partials = new ArrayList<>();
        for (int i = 0; i < texts.size(); i += aiBatchSize) {
            List<String> batch = texts.subList(i, Math.min(i + aiBatchSize, texts.size()));
            AiAnalysisResult result = aiAnalysisClient.analyzeBatch(batch, snack.getName());
            if (result != null) partials.add(result);
        }

        if (partials.isEmpty()) {
            log.warn("[4단계] '{}' AI 분석 결과 0건", snack.getName());
            return;
        }

        // 평균 합산
        AiAnalysisResult merged = mergeResults(partials);

        // PostgreSQL snack_analysis 저장(or 업데이트)
        SnackAnalysis entity = snackAnalysisRepository.findById(snack.getId())
                .orElseGet(() -> SnackAnalysis.builder().snackId(snack.getId()).snack(snack).build());
        entity.setSweetScore(merged.getSweetScore());
        entity.setSaltyScore(merged.getSaltyScore());
        entity.setSpicyScore(merged.getSpicyScore());
        entity.setCrispyScore(merged.getCrispyScore());
        entity.setSoftScore(merged.getSoftScore());
        entity.setPositiveRatio(merged.getPositiveRatio());
        entity.setNegativeRatio(merged.getNegativeRatio());
        entity.setTopKeywords(String.join(",", merged.getTopKeywords()));
        entity.setUpdatedAt(LocalDateTime.now());
        snackAnalysisRepository.save(entity);

        log.info("[4단계] '{}' 분석 결과 저장 (배치 {}회 합산)", snack.getName(), partials.size());
    }

    /** N개 부분 결과를 단순 평균으로 합산. 키워드는 빈도순 상위 5개 */
    private AiAnalysisResult mergeResults(List<AiAnalysisResult> list) {
        int n = list.size();
        double sweet = 0, salty = 0, spicy = 0, crispy = 0, soft = 0;
        int pos = 0, neg = 0;
        java.util.Map<String, Integer> keywordFreq = new java.util.HashMap<>();

        for (AiAnalysisResult r : list) {
            sweet  += nz(r.getSweetScore());
            salty  += nz(r.getSaltyScore());
            spicy  += nz(r.getSpicyScore());
            crispy += nz(r.getCrispyScore());
            soft   += nz(r.getSoftScore());
            pos    += r.getPositiveRatio() != null ? r.getPositiveRatio() : 0;
            neg    += r.getNegativeRatio() != null ? r.getNegativeRatio() : 0;
            if (r.getTopKeywords() != null) {
                for (String k : r.getTopKeywords()) {
                    if (k != null && !k.isBlank()) {
                        keywordFreq.merge(k.trim(), 1, Integer::sum);
                    }
                }
            }
        }

        List<String> topKeywords = keywordFreq.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .map(java.util.Map.Entry::getKey)
                .toList();

        return AiAnalysisResult.builder()
                .sweetScore(round1(sweet / n))
                .saltyScore(round1(salty / n))
                .spicyScore(round1(spicy / n))
                .crispyScore(round1(crispy / n))
                .softScore(round1(soft / n))
                .positiveRatio(pos / n)
                .negativeRatio(neg / n)
                .topKeywords(topKeywords)
                .build();
    }

    // =============================================================
    // 유틸
    // =============================================================
    private double nz(Double d) { return d == null ? 0.0 : d; }
    private double round1(double v) { return Math.round(v * 10) / 10.0; }

    private Long parseLong(String s) {
        try { return s == null ? null : Long.parseLong(s); }
        catch (Exception e) { return null; }
    }

    private LocalDateTime parseIsoDate(String iso) {
        try { return iso == null ? null : OffsetDateTime.parse(iso).toLocalDateTime(); }
        catch (Exception e) { return null; }
    }
}
