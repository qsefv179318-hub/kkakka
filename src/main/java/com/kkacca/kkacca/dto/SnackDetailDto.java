package com.kkacca.kkacca.dto;

import com.kkacca.kkacca.entity.Snack;
import com.kkacca.kkacca.entity.SnackAnalysis;
import lombok.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class SnackDetailDto {
    private Long snackId;
    private String name;
    private String imageUrl;
    private String manufacturer;
    private Double averageRating;

    private TasteScoreDto tasteScores;
    private Integer positiveRatio;
    private Integer negativeRatio;
    private List<String> topKeywords;

    /** 정상 케이스 매핑 */
    public static SnackDetailDto of(Snack snack, SnackAnalysis a) {
        return SnackDetailDto.builder()
                .snackId(snack.getId())
                .name(snack.getName())
                .imageUrl(snack.getImageUrl())
                .manufacturer(snack.getManufacturer())
                .averageRating(snack.getAverageRating())
                .tasteScores(TasteScoreDto.from(a))
                .positiveRatio(a.getPositiveRatio())
                .negativeRatio(a.getNegativeRatio())
                .topKeywords(Arrays.asList(a.getTopKeywords().split(",")))
                .build();
    }

    /** ★ Fallback: snack_analysis 레코드가 없을 때 (설계서 4번 핵심 정책) */
    public static SnackDetailDto fallback(Snack snack) {
        return SnackDetailDto.builder()
                .snackId(snack.getId())
                .name(snack.getName())
                .imageUrl(snack.getImageUrl())
                .manufacturer(snack.getManufacturer())
                .averageRating(snack.getAverageRating())
                .tasteScores(TasteScoreDto.empty())
                .positiveRatio(0)
                .negativeRatio(0)
                .topKeywords(Collections.singletonList("분석 준비 중"))
                .build();
    }
}
