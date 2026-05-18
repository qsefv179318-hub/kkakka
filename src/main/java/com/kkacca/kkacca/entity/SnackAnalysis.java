package com.kkacca.kkacca.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "snack_analysis")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SnackAnalysis {
    @Id
    private Long snackId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "snack_id")
    private Snack snack;

    @Column(nullable = false) private Double sweetScore;
    @Column(nullable = false) private Double saltyScore;
    @Column(nullable = false) private Double spicyScore;
    @Column(nullable = false) private Double crispyScore;
    @Column(nullable = false) private Double softScore;   // ★ V2 추가

    @Column(nullable = false) private Integer positiveRatio;
    @Column(nullable = false) private Integer negativeRatio;

    @Column(nullable = false, length = 255)
    private String topKeywords;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
