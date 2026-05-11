package com.hnust.easyweibo.backend.config;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import com.hnust.easyweibo.backend.security.JwtService;

@Component
public class ChatSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public ChatSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        String token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        if (token == null || token.isBlank()) {
            return false;
        }

        Long userId = jwtService.parseUserId("Bearer " + token.trim());
        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Exception exception
    ) {
        // No-op
    }
}
