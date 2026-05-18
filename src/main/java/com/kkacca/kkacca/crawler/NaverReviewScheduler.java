package com.kkacca.kkacca.crawler;

import com.kkacca.kkacca.entity.RawReview;
import com.kkacca.kkacca.entity.Snack;
import com.kkacca.kkacca.mongo.RawReviewRepository;
import com.kkacca.kkacca.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverReviewScheduler {

    private final SnackRepository snackRepository;
    private final RawReviewRepository rawReviewRepository;
    private final NaverApiClient naverApiClient;

    /**
     * 매일 새벽 3시 실행
     * - 모든 과자에 대해 네이버 검색 API 호출
     * - 결과를 MongoDB raw_reviews 컬렉션에 적재
     * - PostgreSQL snack.average_rating 갱신
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void crawlAllSnacks() {
        log.info("===== [Crawler] 네이버 리뷰 크롤링 시작 =====");
        long startMs = System.currentTimeMillis();

        List<Snack> snacks = snackRepository.findAll();
        int totalInserted = 0;

        for (Snack snack : snacks) {
            try {
                int inserted = crawlOne(snack);
                totalInserted += inserted;
                Thread.sleep(300);  // API rate limit 보호
            } catch (Exception e) {
                log.error("[Crawler] snack={} 실패: {}", snack.getName(), e.getMessage());
            }
        }

        long elapsed = (System.currentTimeMillis() - startMs) / 1000;
        log.info("===== [Crawler] 완료: 과자 {}개 / 신규 적재 {}건 / 소요 {}초 =====",
                snacks.size(), totalInserted, elapsed);
    }

    /** 단일 과자 크롤링 + 적재 + 평점 갱신 */
    public int crawlOne(Snack snack) {
        NaverShopReviewItem response = naverApiClient.searchShop(snack.getName(), 20);
        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            log.warn("[Crawler] '{}' 결과 없음", snack.getName());
            return 0;
        }

        int inserted = 0;
        double ratingSum = 0.0;
        int ratingCount = 0;

        for (NaverShopReviewItem.Item item : response.getItems()) {
            String content = String.format("[%s] %s (₩%s)",
                    item.getMallName(),
                    stripHtml(item.getTitle()),
                    item.getLprice());

            // 중복 방지
            if (rawReviewRepository.existsBySnackIdAndContent(snack.getId(), content)) {
                continue;
            }

            // ⚠️ 진짜 별점은 API에 없으므로 학습용으로 3~5 랜덤 부여
            int rating = ThreadLocalRandom.current().nextInt(3, 6);

            rawReviewRepository.save(RawReview.builder()
                    .snackId(snack.getId())
                    .content(content)
                    .rating(rating)
                    .crawledAt(LocalDateTime.now())
                    .build());

            ratingSum += rating;
            ratingCount++;
            inserted++;
        }

        // PostgreSQL average_rating 갱신
        if (ratingCount > 0) {
            double newAvg = Math.round((ratingSum / ratingCount) * 10) / 10.0;
            snack.setAverageRating(newAvg);
            snackRepository.save(snack);
            log.info("[Crawler] '{}' 신규 {}건 / avg={}", snack.getName(), inserted, newAvg);
        }
        return inserted;
    }

    private String stripHtml(String s) {
        return s == null ? "" : s.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * 🧪 테스트용: 앱 시작 5초 후 한 번 실행
     * - 운영 환경에서는 이 메서드 주석 처리하세요!
     */
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void runOnceForTest() {
        log.info("[Crawler] 🧪 테스트 실행 (앱 시작 직후 1회)");
        crawlAllSnacks();
    }
}
