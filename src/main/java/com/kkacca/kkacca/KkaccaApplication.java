package com.kkacca.kkacca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.kkacca.kkacca.repository") // JPA는 여기만 봐라!
@EnableMongoRepositories(basePackages = "com.kkacca.kkacca.mongo")      // Mongo는 여기만 봐라!
public class KkaccaApplication {
    public static void main(String[] args) {
        SpringApplication.run(KkaccaApplication.class, args);
    }
}