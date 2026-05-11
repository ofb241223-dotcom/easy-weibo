package com.hnust.easyweibo.backend.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.hnust.easyweibo.backend.domain.dto.ai.AiConversationDetailResponse;
import com.hnust.easyweibo.backend.domain.dto.ai.AiConversationResponse;
import com.hnust.easyweibo.backend.domain.dto.ai.AiStreamMessageRequest;
import com.hnust.easyweibo.backend.service.AiChatService;
import com.hnust.easyweibo.backend.service.AuthService;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiChatService aiChatService;
    private final AuthService authService;

    public AiController(AiChatService aiChatService, AuthService authService) {
        this.aiChatService = aiChatService;
        this.authService = authService;
    }

    @GetMapping("/conversations")
    public List<AiConversationResponse> getConversations(@RequestHeader("Authorization") String authorization) {
        Long currentUserId = authService.requireUserId(authorization);
        return aiChatService.getConversations(currentUserId);
    }

    @PostMapping("/conversations")
    public AiConversationDetailResponse createConversation(@RequestHeader("Authorization") String authorization) {
        Long currentUserId = authService.requireUserId(authorization);
        return aiChatService.createConversation(currentUserId);
    }

    @GetMapping("/conversations/{id}")
    public AiConversationDetailResponse getConversation(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return aiChatService.getConversation(id, currentUserId);
    }

    @DeleteMapping("/conversations/{id}")
    public java.util.Map<String, String> deleteConversation(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        aiChatService.deleteConversation(id, currentUserId);
        return java.util.Map.of("message", "会话已删除");
    }

    @PostMapping(value = "/conversations/{id}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
        @PathVariable("id") Long id,
        @Valid @RequestBody AiStreamMessageRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return aiChatService.streamMessage(id, currentUserId, request.message(), request.model());
    }

    @PostMapping(value = "/conversations/{id}/retry", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter retry(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return aiChatService.retry(id, currentUserId);
    }
}
