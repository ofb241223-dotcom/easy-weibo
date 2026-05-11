package com.hnust.easyweibo.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.hnust.easyweibo.backend.domain.dto.chat.ChatConversationDetailResponse;
import com.hnust.easyweibo.backend.domain.dto.chat.ChatConversationResponse;
import com.hnust.easyweibo.backend.domain.dto.chat.ChatMessageResponse;
import com.hnust.easyweibo.backend.domain.dto.chat.CreateConversationRequest;
import com.hnust.easyweibo.backend.domain.dto.chat.SendMessageRequest;
import com.hnust.easyweibo.backend.service.AuthService;
import com.hnust.easyweibo.backend.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    public ChatController(ChatService chatService, AuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    @GetMapping("/conversations")
    public List<ChatConversationResponse> getConversations(@RequestHeader("Authorization") String authorization) {
        Long currentUserId = authService.requireUserId(authorization);
        return chatService.getConversations(currentUserId);
    }

    @PostMapping("/conversations")
    public ChatConversationResponse createConversation(
        @Valid @RequestBody CreateConversationRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return chatService.createConversation(currentUserId, request.targetUserId());
    }

    @GetMapping("/conversations/{id}")
    public ChatConversationDetailResponse getConversation(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return chatService.getConversationDetail(id, currentUserId);
    }

    @PostMapping("/conversations/{id}/messages")
    public ChatMessageResponse sendMessage(
        @PathVariable("id") Long id,
        @Valid @RequestBody SendMessageRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return chatService.sendMessage(id, currentUserId, request);
    }

    @PostMapping("/conversations/{id}/read")
    public Map<String, String> markConversationRead(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        chatService.markConversationRead(id, currentUserId);
        return Map.of("message", "会话已标记为已读");
    }

    @PostMapping("/messages/{id}/recall")
    public ChatMessageResponse recallMessage(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return chatService.recallMessage(id, currentUserId);
    }

    @PostMapping("/users/{id}/block")
    public Map<String, String> blockUser(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        chatService.blockUser(currentUserId, id);
        return Map.of("message", "已拉黑该用户");
    }

    @DeleteMapping("/users/{id}/block")
    public Map<String, String> unblockUser(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        chatService.unblockUser(currentUserId, id);
        return Map.of("message", "已解除拉黑");
    }
}
