package com.kkacca.kkacca.crawler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverShopReviewItem {
    private int total;
    private List<Item> items;

    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String title;       // 상품명 (HTML 태그 포함 — strip 필요)
        private String lprice;      // 최저가
        private String mallName;    // 판매처
        private String category4;   // 세부 카테고리
        private String brand;
        private String maker;
    }
}
