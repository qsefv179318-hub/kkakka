package com.kkacca.kkacca.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 댓글 묶음을 OpenAI Chat Completions API로 보내 분석 결과(JSON)를 받는 클라이언트
 * - 알고리즘 4단계의 "댓글 50개씩 → AI 호출" 부분 담당
 * - 응답을 JSON 객체로 강제 (response_format: json_object)
 */
@Slf4j
@Component
public class AiAnalysisClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public AiAnalysisClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${ai.openai-url}") String apiUrl,
            @Value("${ai.model}") String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.webClient = WebClient.builder().build();
    }

    /**
     * 댓글 묶음 1개를 AI에 보내 분석 결과 1개를 반환
     * @param comments 분석할 댓글 본문 리스트 (50개 단위 권장)
     * @param snackName 과자 이름 (프롬프트 컨텍스트용)
     */
    public AiAnalysisResult analyzeBatch(List<String> comments, String snackName) {
        if (comments == null || comments.isEmpty()) return null;

        String systemPrompt = """
                너는 한국 과자에 대한 유튜브 댓글을 분석하는 평가자다.
                반드시 아래 JSON 스키마로만 응답하라. 설명 금지.
                {
                  "positiveRatio": 0~100 정수,
                  "negativeRatio": 0~100 정수,
                  "sweetScore": 0~10 실수,
                  "saltyScore": 0~10 실수,
                  "spicyScore": 0~10 실수,
                  "crispyScore": 0~10 실수,
                  "softScore": 0~10 실수,
                  "topKeywords": ["키워드1","키워드2","키워드3","키워드4","키워드5"]
                }
                """;

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("과자명: ").append(snackName).append("\n\n댓글 목록:\n");
        for (int i = 0; i < comments.size(); i++) {
            userPrompt.append(i + 1).append(". ").append(comments.get(i)).append("\n");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("response_format", Map.of("type", "json_object"));
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt.toString())
        ));
        body.put("temperature", 0.2);

        try {
            String responseJson = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseJson);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readValue(content, AiAnalysisResult.class);
        } catch (Exception e) {
            log.error("[AI] 분석 실패 snack={}, batchSize={}: {}",
                    snackName, comments.size(), e.getMessage());
            return null;
        }
    }
}
