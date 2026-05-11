package com.hnust.easyweibo.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.hnust.easyweibo.backend.domain.dto.ai.AiConversationDetailResponse;
import com.hnust.easyweibo.backend.domain.dto.ai.AiConversationResponse;
import com.hnust.easyweibo.backend.domain.dto.ai.AiMessageResponse;
import com.hnust.easyweibo.backend.domain.entity.AiConversationEntity;
import com.hnust.easyweibo.backend.domain.entity.AiMessageEntity;
import com.hnust.easyweibo.backend.exception.ApiException;
import com.hnust.easyweibo.backend.mapper.AiConversationMapper;
import com.hnust.easyweibo.backend.mapper.AiMessageMapper;

@Service
public class AiChatService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String DEFAULT_TITLE = "新对话";

    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final GeminiClient geminiClient;

    public AiChatService(
        AiConversationMapper conversationMapper,
        AiMessageMapper messageMapper,
        GeminiClient geminiClient
    ) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.geminiClient = geminiClient;
    }

    public List<AiConversationResponse> getConversations(Long userId) {
        return conversationMapper.findByUserId(userId).stream()
            .map(this::toConversationResponse)
            .toList();
    }

    public AiConversationDetailResponse createConversation(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        AiConversationEntity conversation = new AiConversationEntity();
        conversation.setUserId(userId);
        conversation.setTitle(DEFAULT_TITLE);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        conversationMapper.insert(conversation);
        return toDetailResponse(conversation, List.of());
    }

    public AiConversationDetailResponse getConversation(Long id, Long userId) {
        AiConversationEntity conversation = requireConversation(id, userId);
        return toDetailResponse(conversation, messageMapper.findByConversationId(id));
    }

    public void deleteConversation(Long id, Long userId) {
        requireConversation(id, userId);
        int deleted = conversationMapper.deleteByIdAndUserId(id, userId);
        if (deleted == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "会话不存在");
        }
    }

    public SseEmitter streamMessage(Long conversationId, Long userId, String rawMessage, String requestedModel) {
        AiConversationEntity conversation = requireConversation(conversationId, userId);
        String message = rawMessage.trim();
        String model = geminiClient.normalizeModel(requestedModel);
        LocalDateTime now = LocalDateTime.now();

        AiMessageEntity userMessage = new AiMessageEntity();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("user");
        userMessage.setContent(message);
        userMessage.setModel(model);
        userMessage.setCreatedAt(now);
        messageMapper.insert(userMessage);

        maybeRenameConversation(conversation, message);
        touchConversation(conversationId, now);

        List<AiMessageEntity> history = messageMapper.findByConversationId(conversationId);
        return createEmitter(conversationId, history, model);
    }

    public SseEmitter retry(Long conversationId, Long userId) {
        requireConversation(conversationId, userId);
        List<AiMessageEntity> history = messageMapper.findByConversationId(conversationId);
        if (history.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "当前会话还没有可重试的消息");
        }

        AiMessageEntity lastMessage = history.get(history.size() - 1);
        if ("assistant".equals(lastMessage.getRole())) {
            messageMapper.deleteById(lastMessage.getId());
            history = messageMapper.findByConversationId(conversationId);
        }

        if (history.isEmpty() || !"user".equals(history.get(history.size() - 1).getRole())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "当前会话没有可重试的用户消息");
        }

        String model = geminiClient.normalizeModel(history.get(history.size() - 1).getModel());
        touchConversation(conversationId, LocalDateTime.now());
        return createEmitter(conversationId, history, model);
    }

    private SseEmitter createEmitter(Long conversationId, List<AiMessageEntity> history, String model) {
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            try {
                String responseText = geminiClient.streamConversation(history, model, delta ->
                    sendEvent(emitter, java.util.Map.of("type", "delta", "text", delta))
                );

                AiMessageEntity assistantMessage = new AiMessageEntity();
                assistantMessage.setConversationId(conversationId);
                assistantMessage.setRole("assistant");
                assistantMessage.setContent(responseText);
                assistantMessage.setModel(model);
                assistantMessage.setCreatedAt(LocalDateTime.now());
                messageMapper.insert(assistantMessage);
                touchConversation(conversationId, assistantMessage.getCreatedAt());

                sendEvent(emitter, java.util.Map.of(
                    "type", "done",
                    "conversationId", String.valueOf(conversationId),
                    "messageId", String.valueOf(assistantMessage.getId())
                ));
                emitter.complete();
            } catch (ApiException exception) {
                sendEvent(emitter, java.util.Map.of("type", "error", "message", exception.getMessage()));
                emitter.complete();
            } catch (Exception exception) {
                sendEvent(emitter, java.util.Map.of("type", "error", "message", "AI 请求失败，请稍后重试"));
                emitter.complete();
            }
        });

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, Object payload) {
        try {
            emitter.send(SseEmitter.event().data(payload));
        } catch (Exception ignored) {
            emitter.completeWithError(ignored);
        }
    }

    private void touchConversation(Long conversationId, LocalDateTime updatedAt) {
        AiConversationEntity conversation = new AiConversationEntity();
        conversation.setId(conversationId);
        conversation.setUpdatedAt(updatedAt);
        conversationMapper.touchUpdatedAt(conversation);
    }

    private void maybeRenameConversation(AiConversationEntity conversation, String message) {
        if (!DEFAULT_TITLE.equals(conversation.getTitle())) {
            return;
        }

        String normalized = message.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return;
        }

        String title = normalized.length() > 18 ? normalized.substring(0, 18) + "…" : normalized;
        conversation.setTitle(title);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.updateTitleAndTouched(conversation);
    }

    private AiConversationEntity requireConversation(Long id, Long userId) {
        AiConversationEntity conversation = conversationMapper.findByIdAndUserId(id, userId);
        if (conversation == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "会话不存在");
        }
        return conversation;
    }

    private AiConversationResponse toConversationResponse(AiConversationEntity conversation) {
        List<AiMessageEntity> messages = messageMapper.findByConversationId(conversation.getId());
        String preview = messages.isEmpty() ? "" : messages.get(messages.size() - 1).getContent();
        if (preview.length() > 48) {
            preview = preview.substring(0, 48) + "…";
        }

        return new AiConversationResponse(
            String.valueOf(conversation.getId()),
            conversation.getTitle(),
            conversation.getCreatedAt().format(TIME_FORMATTER),
            conversation.getUpdatedAt().format(TIME_FORMATTER),
            preview
        );
    }

    private AiConversationDetailResponse toDetailResponse(AiConversationEntity conversation, List<AiMessageEntity> messages) {
        return new AiConversationDetailResponse(
            String.valueOf(conversation.getId()),
            conversation.getTitle(),
            conversation.getCreatedAt().format(TIME_FORMATTER),
            conversation.getUpdatedAt().format(TIME_FORMATTER),
            messages.stream().map(this::toMessageResponse).toList()
        );
    }

    private AiMessageResponse toMessageResponse(AiMessageEntity message) {
        return new AiMessageResponse(
            String.valueOf(message.getId()),
            message.getRole(),
            message.getContent(),
            message.getCreatedAt().format(TIME_FORMATTER),
            geminiClient.normalizeModel(message.getModel())
        );
    }
}
