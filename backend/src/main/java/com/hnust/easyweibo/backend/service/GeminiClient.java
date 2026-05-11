package com.hnust.easyweibo.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hnust.easyweibo.backend.config.AppProperties;
import com.hnust.easyweibo.backend.domain.entity.AiMessageEntity;
import com.hnust.easyweibo.backend.exception.ApiException;

@Service
public class GeminiClient {

    public static final String MODEL_FLASH_LITE = "gemini-2.5-flash-lite";
    public static final String MODEL_FLASH = "gemini-2.5-flash";
    public static final String MODEL_PRO = "gemini-2.5-pro";

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiClient(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    }

    public String streamConversation(List<AiMessageEntity> history, String requestedModel, Consumer<String> onDelta) {
        String apiKey = appProperties.getGemini().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI 服务未配置，请先设置 GEMINI_API_KEY");
        }

        try {
            String model = normalizeModel(requestedModel);
            JsonNode payload = buildPayload(history);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                    + model
                    + ":streamGenerateContent?alt=sse"))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .timeout(Duration.ofMinutes(2))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();

            HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new ApiException(HttpStatus.BAD_GATEWAY, extractErrorMessage(body));
            }

            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || !line.startsWith("data:")) {
                        continue;
                    }

                    String raw = line.substring(5).trim();
                    if ("[DONE]".equals(raw)) {
                        break;
                    }

                    JsonNode root = objectMapper.readTree(raw);
                    JsonNode candidates = root.path("candidates");
                    if (!candidates.isArray()) {
                        continue;
                    }

                    for (JsonNode candidate : candidates) {
                        JsonNode parts = candidate.path("content").path("parts");
                        if (!parts.isArray()) {
                            continue;
                        }
                        for (JsonNode part : parts) {
                            String text = part.path("text").asText("");
                            if (!text.isBlank()) {
                                builder.append(text);
                                onDelta.accept(text);
                            }
                        }
                    }
                }
            }

            String result = builder.toString().trim();
            if (result.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI 没有返回可用内容，请稍后重试");
            }
            return result;
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 请求被中断，请稍后重试");
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI 响应解析失败，请稍后重试");
        }
    }

    private JsonNode buildPayload(List<AiMessageEntity> history) {
        var root = objectMapper.createObjectNode();
        var contents = root.putArray("contents");
        for (AiMessageEntity message : history) {
            var content = contents.addObject();
            content.put("role", "assistant".equals(message.getRole()) ? "model" : "user");
            var parts = content.putArray("parts");
            parts.addObject().put("text", message.getContent());
        }
        return root;
    }

    public String normalizeModel(String requestedModel) {
        String model = requestedModel == null || requestedModel.isBlank()
            ? appProperties.getGemini().getModel()
            : requestedModel.trim();

        return switch (model) {
            case MODEL_FLASH_LITE, MODEL_FLASH, MODEL_PRO -> model;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "不支持的 AI 模型");
        };
    }

    private String extractErrorMessage(String body) {
        try {
            JsonNode json = objectMapper.readTree(body);
            String message = json.path("error").path("message").asText("");
            return message.isBlank() ? "AI 服务暂时不可用，请稍后重试" : message;
        } catch (Exception ignored) {
            return "AI 服务暂时不可用，请稍后重试";
        }
    }
}
