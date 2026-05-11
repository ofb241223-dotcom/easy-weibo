package com.hnust.easyweibo.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.domain.dto.chat.ChatConversationDetailResponse;
import com.hnust.easyweibo.backend.domain.dto.chat.ChatConversationResponse;
import com.hnust.easyweibo.backend.domain.dto.chat.ChatMessageResponse;
import com.hnust.easyweibo.backend.domain.dto.chat.SendMessageRequest;
import com.hnust.easyweibo.backend.domain.entity.ConversationEntity;
import com.hnust.easyweibo.backend.domain.entity.MessageEntity;
import com.hnust.easyweibo.backend.domain.entity.UserEntity;
import com.hnust.easyweibo.backend.exception.ApiException;
import com.hnust.easyweibo.backend.mapper.ChatMapper;

@Service
public class ChatService {

    private static final Duration RECALL_WINDOW = Duration.ofMinutes(2);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String TYPE_TEXT = "TEXT";
    private static final String TYPE_IMAGE = "IMAGE";
    private static final String TYPE_FILE = "FILE";

    private final ChatMapper chatMapper;
    private final UserService userService;
    private final ChatRealtimeService chatRealtimeService;

    public ChatService(ChatMapper chatMapper, UserService userService, ChatRealtimeService chatRealtimeService) {
        this.chatMapper = chatMapper;
        this.userService = userService;
        this.chatRealtimeService = chatRealtimeService;
    }

    public List<ChatConversationResponse> getConversations(Long currentUserId) {
        return chatMapper.findConversationsByUserId(currentUserId).stream()
            .map(conversation -> toConversationResponse(conversation, currentUserId))
            .toList();
    }

    public ChatConversationResponse createConversation(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能和自己发起聊天");
        }
        userService.getEntityById(targetUserId);
        ConversationEntity conversation = findOrCreateConversation(currentUserId, targetUserId);
        return toConversationResponse(conversation, currentUserId);
    }

    public ChatConversationDetailResponse getConversationDetail(Long conversationId, Long currentUserId) {
        ConversationEntity conversation = requireConversationParticipant(conversationId, currentUserId);
        Long targetUserId = getTargetUserId(conversation, currentUserId);
        UserEntity targetUser = userService.getEntityById(targetUserId);
        ChatPermission permission = getPermission(conversation, currentUserId, targetUserId);
        int unreadCount = chatMapper.countUnreadMessages(conversationId, currentUserId);

        return new ChatConversationDetailResponse(
            String.valueOf(conversation.getId()),
            userService.toResponse(targetUser, currentUserId),
            unreadCount,
            permission.blockedByCurrentUser(),
            permission.blockedByOtherUser(),
            permission.canSend(),
            permission.restrictionReason(),
            chatMapper.findMessagesByConversationId(conversationId).stream()
                .map(message -> toMessageResponse(message, currentUserId))
                .toList()
        );
    }

    public ChatMessageResponse sendMessage(Long conversationId, Long currentUserId, SendMessageRequest request) {
        userService.assertCanCreateContent(currentUserId);
        ConversationEntity conversation = requireConversationParticipant(conversationId, currentUserId);
        Long targetUserId = getTargetUserId(conversation, currentUserId);
        userService.getEntityById(targetUserId);

        ChatPermission permission = getPermission(conversation, currentUserId, targetUserId);
        if (!permission.canSend()) {
            throw new ApiException(HttpStatus.FORBIDDEN, permission.restrictionReason());
        }

        String messageType = normalizeMessageType(request.messageType());
        String trimmedContent = request.content() == null ? "" : request.content().trim();

        if (TYPE_TEXT.equals(messageType)) {
            if (trimmedContent.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "消息内容不能为空");
            }
            if (trimmedContent.length() > 2000) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "消息内容不能超过 2000 个字符");
            }
        } else {
            if ((request.fileUrl() == null || request.fileUrl().isBlank()) || (request.fileName() == null || request.fileName().isBlank())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "附件消息缺少文件信息");
            }
            if (!trimmedContent.isBlank() && trimmedContent.length() > 2000) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "消息内容不能超过 2000 个字符");
            }
        }

        MessageEntity message = new MessageEntity();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setContent(trimmedContent.isBlank() ? null : trimmedContent);
        message.setMessageType(messageType);
        message.setFileUrl(blankToNull(request.fileUrl()));
        message.setFileName(blankToNull(request.fileName()));
        message.setMimeType(blankToNull(request.mimeType()));
        message.setCreatedAt(LocalDateTime.now());
        message.setReadAt(null);
        message.setRecalled(false);
        message.setRecalledAt(null);
        chatMapper.insertMessage(message);
        chatMapper.touchConversation(conversationId, message.getCreatedAt());
        ChatMessageResponse response = toMessageResponse(chatMapper.findMessageById(message.getId()), currentUserId);
        chatRealtimeService.publishMessageCreated(conversationId, participants(conversation), response);
        return response;
    }

    public void markConversationRead(Long conversationId, Long currentUserId) {
        ConversationEntity conversation = requireConversationParticipant(conversationId, currentUserId);
        chatMapper.markConversationAsRead(conversationId, currentUserId, LocalDateTime.now());
        chatRealtimeService.publishConversationInvalidated(conversationId, participants(conversation));
    }

    public ChatMessageResponse recallMessage(Long messageId, Long currentUserId) {
        MessageEntity message = chatMapper.findMessageById(messageId);
        if (message == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "消息不存在");
        }
        requireConversationParticipant(message.getConversationId(), currentUserId);
        if (!message.getSenderId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只能撤回自己发送的消息");
        }
        if (Boolean.TRUE.equals(message.getRecalled())) {
            throw new ApiException(HttpStatus.CONFLICT, "这条消息已经撤回");
        }
        if (Duration.between(message.getCreatedAt(), LocalDateTime.now()).compareTo(RECALL_WINDOW) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "消息发送超过 2 分钟，不能再撤回");
        }

        LocalDateTime recalledAt = LocalDateTime.now();
        chatMapper.recallMessage(messageId, recalledAt);
        chatMapper.touchConversation(message.getConversationId(), recalledAt);
        ConversationEntity conversation = requireConversationParticipant(message.getConversationId(), currentUserId);
        ChatMessageResponse response = toMessageResponse(chatMapper.findMessageById(messageId), currentUserId);
        chatRealtimeService.publishMessageRecalled(message.getConversationId(), participants(conversation), response);
        return response;
    }

    public void blockUser(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能拉黑自己");
        }
        userService.getEntityById(targetUserId);
        if (!chatMapper.existsBlock(currentUserId, targetUserId)) {
            chatMapper.insertBlock(currentUserId, targetUserId, LocalDateTime.now());
        }
        ConversationEntity conversation = findConversationBetweenUsers(currentUserId, targetUserId);
        if (conversation != null) {
            chatRealtimeService.publishConversationInvalidated(conversation.getId(), participants(conversation));
        }
    }

    public void unblockUser(Long currentUserId, Long targetUserId) {
        chatMapper.deleteBlock(currentUserId, targetUserId);
        ConversationEntity conversation = findConversationBetweenUsers(currentUserId, targetUserId);
        if (conversation != null) {
            chatRealtimeService.publishConversationInvalidated(conversation.getId(), participants(conversation));
        }
    }

    private ConversationEntity findOrCreateConversation(Long leftUserId, Long rightUserId) {
        long userAId = Math.min(leftUserId, rightUserId);
        long userBId = Math.max(leftUserId, rightUserId);
        ConversationEntity existing = chatMapper.findConversationBetween(userAId, userBId);
        if (existing != null) {
            return existing;
        }

        ConversationEntity entity = new ConversationEntity();
        entity.setUserAId(userAId);
        entity.setUserBId(userBId);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        chatMapper.insertConversation(entity);
        return entity;
    }

    private ConversationEntity requireConversationParticipant(Long conversationId, Long currentUserId) {
        ConversationEntity conversation = chatMapper.findConversationById(conversationId);
        if (conversation == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "会话不存在");
        }
        if (!conversation.getUserAId().equals(currentUserId) && !conversation.getUserBId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "不能访问他人的会话");
        }
        return conversation;
    }

    private ConversationEntity findConversationBetweenUsers(Long userAId, Long userBId) {
        long left = Math.min(userAId, userBId);
        long right = Math.max(userAId, userBId);
        return chatMapper.findConversationBetween(left, right);
    }

    private List<Long> participants(ConversationEntity conversation) {
        return List.of(conversation.getUserAId(), conversation.getUserBId());
    }

    private Long getTargetUserId(ConversationEntity conversation, Long currentUserId) {
        return conversation.getUserAId().equals(currentUserId) ? conversation.getUserBId() : conversation.getUserAId();
    }

    private ChatConversationResponse toConversationResponse(ConversationEntity conversation, Long currentUserId) {
        Long targetUserId = getTargetUserId(conversation, currentUserId);
        UserEntity targetUser = userService.getEntityById(targetUserId);
        MessageEntity lastMessage = chatMapper.findLastMessage(conversation.getId());
        ChatPermission permission = getPermission(conversation, currentUserId, targetUserId);
        return new ChatConversationResponse(
            String.valueOf(conversation.getId()),
            userService.toResponse(targetUser, currentUserId),
            lastMessage == null ? "" : previewMessage(lastMessage),
            lastMessage == null ? conversation.getUpdatedAt().format(TIME_FORMATTER) : lastMessage.getCreatedAt().format(TIME_FORMATTER),
            chatMapper.countUnreadMessages(conversation.getId(), currentUserId),
            permission.blockedByCurrentUser(),
            permission.blockedByOtherUser(),
            permission.canSend(),
            permission.restrictionReason()
        );
    }

    private ChatMessageResponse toMessageResponse(MessageEntity message, Long currentUserId) {
        boolean recalled = Boolean.TRUE.equals(message.getRecalled());
        return new ChatMessageResponse(
            String.valueOf(message.getId()),
            String.valueOf(message.getConversationId()),
            String.valueOf(message.getSenderId()),
            userService.getById(message.getSenderId(), currentUserId),
            recalled ? "撤回了一条消息" : (message.getContent() == null ? "" : message.getContent()),
            message.getMessageType() == null ? TYPE_TEXT : message.getMessageType(),
            recalled ? null : message.getFileUrl(),
            recalled ? null : message.getFileName(),
            recalled ? null : message.getMimeType(),
            message.getCreatedAt().format(TIME_FORMATTER),
            message.getReadAt() != null,
            recalled,
            message.getSenderId().equals(currentUserId)
                && !recalled
                && Duration.between(message.getCreatedAt(), LocalDateTime.now()).compareTo(RECALL_WINDOW) <= 0
        );
    }

    private String previewMessage(MessageEntity message) {
        if (Boolean.TRUE.equals(message.getRecalled())) {
            return "撤回了一条消息";
        }
        if (TYPE_IMAGE.equals(message.getMessageType())) {
            return "[图片]";
        }
        if (TYPE_FILE.equals(message.getMessageType())) {
            return message.getFileName() == null ? "[文件]" : "[文件] " + message.getFileName();
        }
        String content = message.getContent() == null ? "" : message.getContent().trim();
        return content.length() <= 36 ? content : content.substring(0, 36) + "...";
    }

    private String normalizeMessageType(String value) {
        if (value == null || value.isBlank()) {
            return TYPE_TEXT;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case TYPE_TEXT, TYPE_IMAGE, TYPE_FILE -> normalized;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "不支持的消息类型");
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ChatPermission getPermission(ConversationEntity conversation, Long currentUserId, Long targetUserId) {
        boolean blockedByCurrent = chatMapper.existsBlock(currentUserId, targetUserId);
        if (blockedByCurrent) {
            return new ChatPermission(false, true, false, "你已拉黑对方，解除拉黑后才能继续聊天");
        }

        boolean blockedByOther = chatMapper.existsBlock(targetUserId, currentUserId);
        if (blockedByOther) {
            return new ChatPermission(false, false, true, "对方已限制你继续发送消息");
        }

        if (userService.isMutualFollow(currentUserId, targetUserId)) {
            return new ChatPermission(true, false, false, null);
        }

        int sentCount = chatMapper.countMessagesBySender(conversation.getId(), currentUserId);
        if (sentCount == 0) {
            return new ChatPermission(true, false, false, null);
        }

        if (chatMapper.existsMessageFromSender(conversation.getId(), targetUserId)) {
            return new ChatPermission(true, false, false, null);
        }

        return new ChatPermission(false, false, false, "陌生人对话最多先发送一条消息，等待对方回复后才能继续");
    }

    private record ChatPermission(
        boolean canSend,
        boolean blockedByCurrentUser,
        boolean blockedByOtherUser,
        String restrictionReason
    ) {
    }
}
