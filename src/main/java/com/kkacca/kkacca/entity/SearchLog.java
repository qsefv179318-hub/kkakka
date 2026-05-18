package com.kkacca.kkacca.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_log",
       indexes = @Index(name = "idx_keyword_time", columnList = "keyword, searchedAt"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private LocalDateTime searchedAt;
}
