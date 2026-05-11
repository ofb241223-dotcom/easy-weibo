package com.hnust.easyweibo.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.domain.dto.notification.NotificationResponse;
import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;
import com.hnust.easyweibo.backend.domain.entity.NotificationEntity;
import com.hnust.easyweibo.backend.mapper.NotificationMapper;
import com.hnust.easyweibo.backend.mapper.UserMapper;

@Service
public class NotificationService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    public NotificationService(NotificationMapper notificationMapper, UserMapper userMapper) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
    }

    public List<NotificationResponse> getByRecipient(Long recipientId) {
        return notificationMapper.findByRecipientId(recipientId).stream()
            .map(notification -> toResponse(notification, recipientId))
            .toList();
    }

    public void markAsRead(Long notificationId, Long recipientId) {
        notificationMapper.markAsRead(notificationId, recipientId);
    }

    public void markAllAsRead(Long recipientId) {
        notificationMapper.markAllAsRead(recipientId);
    }

    public void createLikeNotification(Long actorId, Long recipientId, Long postId) {
        if (actorId.equals(recipientId)) {
            return;
        }
        notificationMapper.deleteByTypeAndRecipientAndActorAndPost("like", recipientId, actorId, postId);
        notificationMapper.insert(newNotification("like", recipientId, actorId, postId));
    }

    public void removeLikeNotification(Long actorId, Long recipientId, Long postId) {
        notificationMapper.deleteByTypeAndRecipientAndActorAndPost("like", recipientId, actorId, postId);
    }

    public void createCommentNotification(Long actorId, Long recipientId, Long postId) {
        if (actorId.equals(recipientId)) {
            return;
        }
        notificationMapper.insert(newNotification("comment", recipientId, actorId, postId));
    }

    public void createFollowNotification(Long actorId, Long recipientId) {
        if (actorId.equals(recipientId)) {
            return;
        }
        notificationMapper.deleteByTypeAndRecipientAndActor("follow", recipientId, actorId);
        notificationMapper.insert(newNotification("follow", recipientId, actorId, null));
    }

    public void removeFollowNotification(Long actorId, Long recipientId) {
        notificationMapper.deleteByTypeAndRecipientAndActor("follow", recipientId, actorId);
    }

    public void createMentionNotifications(Long actorId, List<Long> recipientIds, Long postId) {
        Set<Long> uniqueRecipientIds = new LinkedHashSet<>(recipientIds);
        for (Long recipientId : uniqueRecipientIds) {
            if (recipientId == null || actorId.equals(recipientId)) {
                continue;
            }
            notificationMapper.insert(newNotification("mention", recipientId, actorId, postId));
        }
    }

    public void createSystemNotification(
        Long actorId,
        Long recipientId,
        Long postId,
        String message,
        String actionLabel,
        String actionUrl
    ) {
        if (actorId == null || recipientId == null) {
            return;
        }
        NotificationEntity notification = newNotification("system", recipientId, actorId, postId);
        notification.setMessage(message);
        notification.setActionLabel(actionLabel);
        notification.setActionUrl(actionUrl);
        notificationMapper.insert(notification);
    }

    private NotificationEntity newNotification(String type, Long recipientId, Long actorId, Long postId) {
        NotificationEntity notification = new NotificationEntity();
        notification.setType(type);
        notification.setRecipientId(recipientId);
        notification.setActorId(actorId);
        notification.setPostId(postId);
        notification.setMessage(null);
        notification.setActionLabel(null);
        notification.setActionUrl(null);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private NotificationResponse toResponse(NotificationEntity notification, Long recipientId) {
        return new NotificationResponse(
            String.valueOf(notification.getId()),
            notification.getType(),
            toUserResponse(notification.getActorId(), recipientId),
            notification.getPostId() == null ? null : String.valueOf(notification.getPostId()),
            notification.getMessage(),
            notification.getActionLabel(),
            notification.getActionUrl(),
            notification.getCreatedAt().format(TIME_FORMATTER),
            notification.getIsRead()
        );
    }

    private UserResponse toUserResponse(Long userId, Long viewerId) {
        var user = userMapper.findById(userId);
        boolean isFollowing = viewerId != null
            && !viewerId.equals(userId)
            && userMapper.existsFollow(viewerId, userId);

        return new UserResponse(
            String.valueOf(user.getId()),
            user.getUsername(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getCoverUrl(),
            user.getBio(),
            user.getRole(),
            user.getStatus(),
            user.getMuteStatus(),
            user.getCreatedAt().format(TIME_FORMATTER),
            userMapper.countFollowers(userId),
            userMapper.countFollowing(userId),
            isFollowing
        );
    }
}
