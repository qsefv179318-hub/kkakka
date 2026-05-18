package com.kkacca.kkacca.crawler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * AI 분석 결과 구조
 * - OpenAI 응답을 JSON 모드로 강제하여 이 형태로 받음
 * - 알고리즘 4단계 합산 대상
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAnalysisResult {
    private Integer positiveRatio;   // 0~100
    private Integer negativeRatio;   // 0~100
    private Double sweetScore;       // 0~10
    private Double saltyScore;
    private Double spicyScore;
    private Double crispyScore;
    private Double softScore;
    private List<String> topKeywords;
}
