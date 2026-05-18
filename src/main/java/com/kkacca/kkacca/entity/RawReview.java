package com.kkacca.kkacca.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "snack")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RawReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private Long snackId;
    private String imageUrl;
    private String manufacturer;
    private Double averageRating;

    @OneToOne(mappedBy = "snack", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SnackAnalysis analysis;
}
