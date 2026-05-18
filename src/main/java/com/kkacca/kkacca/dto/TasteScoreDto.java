package com.kkacca.kkacca.dto;

import com.kkacca.kkacca.entity.SnackAnalysis;
import lombok.*;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class TasteScoreDto {
    private Double sweetScore;
    private Double saltyScore;
    private Double spicyScore;
    private Double crispyScore;
    private Double softScore;   // ★ V2

    public static TasteScoreDto from(SnackAnalysis a) {
        return TasteScoreDto.builder()
                .sweetScore(a.getSweetScore())
                .saltyScore(a.getSaltyScore())
                .spicyScore(a.getSpicyScore())
                .crispyScore(a.getCrispyScore())
                .softScore(a.getSoftScore())
                .build();
    }

    public static TasteScoreDto empty() {
        return TasteScoreDto.builder()
                .sweetScore(0.0).saltyScore(0.0).spicyScore(0.0)
                .crispyScore(0.0).softScore(0.0)
                .build();
    }
}
