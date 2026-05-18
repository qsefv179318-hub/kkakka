package com.kkacca.kkacca.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /** 검색 로그 적재 전용 스레드풀 (메인 요청 스레드와 분리) */
    @Bean(name = "searchLogExecutor")
    public Executor searchLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("SearchLog-");
        executor.initialize();
        log.info("[AsyncConfig] searchLogExecutor 초기화 완료");
        return executor;
    }
}
