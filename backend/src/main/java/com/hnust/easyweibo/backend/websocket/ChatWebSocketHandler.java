package com.hnust.easyweibo.backend.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing user id"));
            return;
        }
        sessionsByUser.computeIfAbsent(userId, ignored -> new CopyOnWriteArraySet<>()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        removeSession(session);
    }

    public void sendToUsers(List<Long> userIds, Object payload) {
        String serialized = toJson(payload);
        if (serialized == null) {
            return;
        }

        userIds.stream()
            .distinct()
            .forEach(userId -> sendToUser(userId, serialized));
    }

    private void sendToUser(Long userId, String payload) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        TextMessage message = new TextMessage(payload);
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }
            try {
                session.sendMessage(message);
            } catch (IOException exception) {
                sessions.remove(session);
            }
        }

        if (sessions.isEmpty()) {
            sessionsByUser.remove(userId);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    private void removeSession(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUser.remove(userId);
        }
    }
}
