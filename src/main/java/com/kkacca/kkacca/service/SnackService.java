package com.kkacca.kkacca.service;

import com.kkacca.kkacca.dto.MainRecommendDto;
import com.kkacca.kkacca.dto.SnackDetailDto;
import com.kkacca.kkacca.dto.SnackSearchDto;
import com.kkacca.kkacca.entity.Snack;
import com.kkacca.kkacca.entity.SnackAnalysis;
import com.kkacca.kkacca.repository.SnackAnalysisRepository;
import com.kkacca.kkacca.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnackService {

    private final SnackRepository snackRepository;
    private final SnackAnalysisRepository analysisRepository;
    private final SearchLogService searchLogService;

    /**
     * [API-1] 메인 카테고리별 1위 조회
     */
    @Cacheable(value = "mainRecommend")
    @Transactional(readOnly = true)
    public List<MainRecommendDto> getMainRecommend() {
        log.info("[Cache MISS] mainRecommend - DB 조회 수행");

        List<MainRecommendDto> result = new ArrayList<>();
        addIfPresent(result, "sweet",  analysisRepository.findTopBySweet(),  SnackAnalysis::getSweetScore);
        addIfPresent(result, "salty",  analysisRepository.findTopBySalty(),  SnackAnalysis::getSaltyScore);
        addIfPresent(result, "spicy",  analysisRepository.findTopBySpicy(),  SnackAnalysis::getSpicyScore);
        addIfPresent(result, "crispy", analysisRepository.findTopByCrispy(), SnackAnalysis::getCrispyScore);
        addIfPresent(result, "soft",   analysisRepository.findTopBySoft(),   SnackAnalysis::getSoftScore);
        return result;
    }

    private void addIfPresent(List<MainRecommendDto> list, String category,
                              Optional<SnackAnalysis> opt,
                              Function<SnackAnalysis, Double> scoreExtractor) {
        opt.ifPresent(a -> {
            Snack snack = a.getSnack();
            list.add(MainRecommendDto.of(category, snack, scoreExtractor.apply(a)));
        });
    }

    /**
     * [API-2] 과자 상세 + AI 통계 조회 (Fallback 정책 포함)
     */
    @Transactional(readOnly = true)
    public SnackDetailDto getSnackDetail(Long snackId) {
        Snack snack = snackRepository.findById(snackId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 과자 ID입니다: " + snackId));

        SnackAnalysis analysis = snack.getAnalysis();
        if (analysis == null) {
            log.warn("[Fallback] snackId={} 의 AI 분석 데이터 없음 - 기본 DTO 반환", snackId);
            return SnackDetailDto.fallback(snack);
        }
        return SnackDetailDto.of(snack, analysis);
    }

    /**
     * [API-3] 과자 검색 + 비동기 로그 적재
     */
    @Transactional(readOnly = true)
    public List<SnackSearchDto> searchSnacks(String keyword) {
        log.info("[Search] keyword='{}'", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색 키워드를 입력해 주세요.");
        }

        List<Snack> snacks = snackRepository.findByNameContainingIgnoreCase(keyword.trim());

        // ★ 비동기 로그 적재 (응답 지연 X)
        searchLogService.saveLog(keyword);

        return snacks.stream()
                .map(SnackSearchDto::from)
                .toList();
    }
}
