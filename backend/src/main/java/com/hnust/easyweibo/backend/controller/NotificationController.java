package com.hnust.easyweibo.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hnust.easyweibo.backend.domain.dto.notification.NotificationResponse;
import com.hnust.easyweibo.backend.service.AuthService;
import com.hnust.easyweibo.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping
    public List<NotificationResponse> listNotifications(@RequestHeader("Authorization") String authorization) {
        Long currentUserId = authService.requireUserId(authorization);
        return notificationService.getByRecipient(currentUserId);
    }

    @PostMapping("/{id}/read")
    public Map<String, String> markAsRead(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        notificationService.markAsRead(id, currentUserId);
        return Map.of("message", "已读");
    }

    @PostMapping("/read-all")
    public Map<String, String> markAllAsRead(@RequestHeader("Authorization") String authorization) {
        Long currentUserId = authService.requireUserId(authorization);
        notificationService.markAllAsRead(currentUserId);
        return Map.of("message", "已全部标记为已读");
    }
}
