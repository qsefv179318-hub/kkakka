package com.kkacca.kkacca.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiAnalysisDto {
    private Double sweetScore;
    private Double saltyScore;
    private Double spicyScore;
    private Double crispyScore;
    private Double softScore;        // ★
    private Integer positiveRatio;
    private Integer negativeRatio;
    private String topKeywords;
}
