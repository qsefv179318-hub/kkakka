package com.kkacca.kkacca.service;

import com.kkacca.kkacca.dto.AiAnalysisDto;
import com.kkacca.kkacca.entity.*;
import com.kkacca.kkacca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SnackService {

    private final SnackRepository snackRepository;
    private final SnackAnalysisRepository analysisRepository;
    private final SearchLogRepository searchLogRepository;

    @Cacheable("mainRecommend")
    public Map<String, SnackAnalysis> getMainRecommend() {
        Map<String, SnackAnalysis> result = new LinkedHashMap<>();
        analysisRepository.findTopBySweet().ifPresent(a -> result.put("sweet", a));
        analysisRepository.findTopBySalty().ifPresent(a -> result.put("salty", a));
        analysisRepository.findTopBySpicy().ifPresent(a -> result.put("spicy", a));
        analysisRepository.findTopByCrispy().ifPresent(a -> result.put("crispy", a));
        analysisRepository.findTopBySoft().ifPresent(a -> result.put("soft", a));
        return result;
    }

    public List<Snack> search(String keyword) {
        List<Snack> snacks = snackRepository.findByNameContainingIgnoreCase(keyword);
        logSearchAsync(keyword);
        return snacks;
    }

    @Async
    public void logSearchAsync(String keyword) {
        if (keyword == null || keyword.isBlank()) return;
        searchLogRepository.save(SearchLog.builder()
                .keyword(keyword)
                .searchedAt(LocalDateTime.now())
                .build());
    }

    public AiAnalysisDto getDetail(Long snackId) {
        Snack snack = snackRepository.findById(snackId)
                .orElseThrow(() -> new IllegalArgumentException("Snack not found: " + snackId));

        SnackAnalysis a = snack.getAnalysis();
        if (a == null) {  // ★ Fallback 정책
            return AiAnalysisDto.builder()
                    .sweetScore(0.0).saltyScore(0.0).spicyScore(0.0)
                    .crispyScore(0.0).softScore(0.0)
                    .positiveRatio(0).negativeRatio(0)
                    .topKeywords("분석 준비 중")
                    .build();
        }
        return AiAnalysisDto.builder()
                .sweetScore(a.getSweetScore()).saltyScore(a.getSaltyScore())
                .spicyScore(a.getSpicyScore()).crispyScore(a.getCrispyScore())
                .softScore(a.getSoftScore())
                .positiveRatio(a.getPositiveRatio()).negativeRatio(a.getNegativeRatio())
                .topKeywords(a.getTopKeywords())
                .build();
    }
}
