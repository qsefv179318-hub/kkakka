package com.kkacca.kkacca.service;

import com.kkacca.kkacca.entity.SearchLog;
import com.kkacca.kkacca.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    /**
     * 검색 로그 비동기 적재
     * - searchLogExecutor 스레드풀에서 실행 (검색 응답 지연 방지)
     */
    @Async("searchLogExecutor")
    public void saveLog(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            log.debug("[SearchLog] 빈 키워드 무시");
            return;
        }
        try {
            SearchLog logEntity = SearchLog.builder()
                    .keyword(keyword.trim())
                    .searchedAt(LocalDateTime.now())
                    .build();
            searchLogRepository.save(logEntity);
            log.info("[SearchLog] 적재 완료: keyword='{}', thread={}",
                    keyword, Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("[SearchLog] 적재 실패: {}", e.getMessage(), e);
        }
    }
}
