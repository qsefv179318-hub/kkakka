package com.kkacca.kkacca.controller;

import com.kkacca.kkacca.dto.MainRecommendDto;
import com.kkacca.kkacca.dto.SnackDetailDto;
import com.kkacca.kkacca.service.SnackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/snacks")
@RequiredArgsConstructor
public class SnackController {

    private final SnackService snackService;

    /**
     * [API-1] 메인 화면 - 5개 카테고리(단/짠/매/바삭/부드러움) 각 1위 조회
     * GET /api/v1/snacks/main/recommend
     */
    @GetMapping("/main/recommend")
    public ResponseEntity<List<MainRecommendDto>> getMainRecommend() {
        return ResponseEntity.ok(snackService.getMainRecommend());
    }

    /**
     * [API-2] 과자 상세 + AI 통계(5대 맛 + 긍부정 + 키워드) 조회
     * GET /api/v1/snacks/{snackId}
     */
    @GetMapping("/{snackId}")
    public ResponseEntity<SnackDetailDto> getSnackDetail(@PathVariable Long snackId) {
        return ResponseEntity.ok(snackService.getSnackDetail(snackId));
    }

    /**
     * 잘못된 snackId가 들어왔을 때(존재하지 않음) 400 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
