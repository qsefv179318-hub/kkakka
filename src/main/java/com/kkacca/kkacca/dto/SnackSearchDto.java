package com.kkacca.kkacca.dto;

import com.kkacca.kkacca.entity.Snack;
import lombok.*;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class SnackSearchDto {
    private Long snackId;
    private String name;
    private String imageUrl;
    private String manufacturer;
    private Double averageRating;

    public static SnackSearchDto from(Snack snack) {
        return SnackSearchDto.builder()
                .snackId(snack.getId())
                .name(snack.getName())
                .imageUrl(snack.getImageUrl())
                .manufacturer(snack.getManufacturer())
                .averageRating(snack.getAverageRating())
                .build();
    }
}
