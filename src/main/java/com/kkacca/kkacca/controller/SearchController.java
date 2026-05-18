package com.kkacca.kkacca.controller;

import com.kkacca.kkacca.dto.SnackSearchDto;
import com.kkacca.kkacca.service.SnackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {

    private final SnackService snackService;

    /**
     * [API-3] 과자 검색 + 검색 로그 비동기 적재
     * GET /api/v1/search?keyword=포카칩
     */
    @GetMapping("/search")
    public ResponseEntity<List<SnackSearchDto>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(snackService.searchSnacks(keyword));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
