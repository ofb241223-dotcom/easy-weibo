package com.hnust.easyweibo.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.hnust.easyweibo.backend.websocket.ChatWebSocketHandler;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ChatSocketAuthInterceptor chatSocketAuthInterceptor;

    public ChatWebSocketConfig(
        ChatWebSocketHandler chatWebSocketHandler,
        ChatSocketAuthInterceptor chatSocketAuthInterceptor
    ) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.chatSocketAuthInterceptor = chatSocketAuthInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(chatWebSocketHandler, "/ws/chat")
            .addInterceptors(chatSocketAuthInterceptor)
            .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*");
    }
}
