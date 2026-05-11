package com.hnust.easyweibo.backend.service;

import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;
import com.hnust.easyweibo.backend.domain.dto.user.UpdateProfileRequest;
import com.hnust.easyweibo.backend.domain.entity.UserEntity;
import com.hnust.easyweibo.backend.exception.ApiException;
import com.hnust.easyweibo.backend.mapper.UserMapper;

@Service
public class UserService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public UserService(UserMapper userMapper, NotificationService notificationService) {
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    public UserEntity getEntityById(Long id) {
        UserEntity user = userMapper.findById(id);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    public UserEntity getEntityByUsername(String username) {
        UserEntity user = userMapper.findByUsername(username);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    public UserEntity findByUsernameOrNull(String username) {
        return userMapper.findByUsername(username);
    }

    public void createUser(UserEntity user) {
        userMapper.insert(user);
    }

    public void updatePassword(Long userId, String encodedPassword) {
        userMapper.updatePassword(userId, encodedPassword);
    }

    public UserResponse getByUsername(String username, Long viewerId) {
        return toResponse(getEntityByUsername(username), viewerId);
    }

    public UserResponse getById(Long id, Long viewerId) {
        return toResponse(getEntityById(id), viewerId);
    }

    public java.util.List<UserResponse> search(String query, Long viewerId) {
        return userMapper.search(query).stream()
            .map(user -> toResponse(user, viewerId))
            .toList();
    }

    public java.util.List<UserResponse> getRecommendedUsers(Long viewerId, int limit) {
        return userMapper.findRecommended(viewerId, limit).stream()
            .map(user -> toResponse(user, viewerId))
            .toList();
    }

    public java.util.List<UserResponse> getFollowingUsers(Long userId, Long viewerId) {
        getEntityById(userId);
        return userMapper.findFollowingUsers(userId).stream()
            .map(user -> toResponse(user, viewerId))
            .toList();
    }

    public java.util.List<UserResponse> getFollowerUsers(Long userId, Long viewerId) {
        getEntityById(userId);
        return userMapper.findFollowerUsers(userId).stream()
            .map(user -> toResponse(user, viewerId))
            .toList();
    }

    public java.util.List<UserResponse> getMutualUsers(Long userId, Long viewerId) {
        getEntityById(userId);
        return userMapper.findMutualUsers(userId).stream()
            .map(user -> toResponse(user, viewerId))
            .toList();
    }

    public java.util.List<UserResponse> getUsersForAdmin(String query, String status, String muteStatus) {
        return userMapper.findForAdmin(query, status, muteStatus).stream()
            .map(user -> toResponse(user, null))
            .toList();
    }

    public UserResponse updateCurrentUser(Long currentUserId, UpdateProfileRequest request) {
        UserEntity user = getEntityById(currentUserId);
        user.setNickname(request.nickname().trim());
        user.setBio(request.bio() == null ? null : request.bio().trim());
        user.setAvatarUrl((request.avatar() == null || request.avatar().isBlank())
            ? user.getAvatarUrl()
            : request.avatar().trim());
        user.setCoverUrl((request.coverUrl() == null || request.coverUrl().isBlank())
            ? user.getCoverUrl()
            : request.coverUrl().trim());
        userMapper.updateProfile(user);
        return toResponse(user, currentUserId);
    }

    public boolean toggleFollow(Long currentUserId, Long targetId) {
        if (currentUserId.equals(targetId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能关注自己");
        }
        getEntityById(targetId);

        if (userMapper.existsFollow(currentUserId, targetId)) {
            userMapper.deleteFollow(currentUserId, targetId);
            notificationService.removeFollowNotification(currentUserId, targetId);
            return false;
        } else {
            userMapper.insertFollow(currentUserId, targetId);
            notificationService.createFollowNotification(currentUserId, targetId);
            return true;
        }
    }

    public void assertCanLogin(UserEntity user) {
        if ("BANNED".equalsIgnoreCase(user.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "账号已被封禁，请联系管理员");
        }
    }

    public void assertCanCreateContent(Long userId) {
        UserEntity user = getEntityById(userId);
        if ("BANNED".equalsIgnoreCase(user.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "账号已被封禁，无法执行该操作");
        }
        if ("MUTED".equalsIgnoreCase(user.getMuteStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "你已被禁言，暂时无法发布内容");
        }
    }

    public void banUser(Long adminUserId, Long targetUserId, String reason) {
        UserEntity target = getModerationTarget(adminUserId, targetUserId);
        userMapper.updateStatus(targetUserId, "BANNED");
        notificationService.createSystemNotification(
            adminUserId,
            targetUserId,
            null,
            "你的账号已被封禁。原因：" + reason,
            null,
            null
        );
    }

    public void unbanUser(Long adminUserId, Long targetUserId, String reason) {
        UserEntity target = getModerationTarget(adminUserId, targetUserId);
        userMapper.updateStatus(targetUserId, "ACTIVE");
        notificationService.createSystemNotification(
            adminUserId,
            targetUserId,
            null,
            "你的账号封禁已解除。说明：" + reason,
            null,
            null
        );
    }

    public void muteUser(Long adminUserId, Long targetUserId, String reason) {
        UserEntity target = getModerationTarget(adminUserId, targetUserId);
        userMapper.updateMuteStatus(targetUserId, "MUTED");
        notificationService.createSystemNotification(
            adminUserId,
            targetUserId,
            null,
            "你已被禁言。原因：" + reason,
            null,
            null
        );
    }

    public void unmuteUser(Long adminUserId, Long targetUserId, String reason) {
        UserEntity target = getModerationTarget(adminUserId, targetUserId);
        userMapper.updateMuteStatus(targetUserId, "NORMAL");
        notificationService.createSystemNotification(
            adminUserId,
            targetUserId,
            null,
            "你的禁言已解除。说明：" + reason,
            null,
            null
        );
    }

    public UserResponse toResponse(UserEntity user, Long viewerId) {
        boolean isFollowing = viewerId != null && !viewerId.equals(user.getId()) && userMapper.existsFollow(viewerId, user.getId());

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
            userMapper.countFollowers(user.getId()),
            userMapper.countFollowing(user.getId()),
            isFollowing
        );
    }

    public boolean isAdmin(Long userId) {
        return "ADMIN".equalsIgnoreCase(getEntityById(userId).getRole());
    }

    public boolean isMutualFollow(Long leftUserId, Long rightUserId) {
        return userMapper.existsFollow(leftUserId, rightUserId) && userMapper.existsFollow(rightUserId, leftUserId);
    }

    public int countUsers() {
        return userMapper.countAll();
    }

    private UserEntity getModerationTarget(Long adminUserId, Long targetUserId) {
        if (adminUserId.equals(targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能对自己执行此操作");
        }

        UserEntity target = getEntityById(targetUserId);
        if ("ADMIN".equalsIgnoreCase(target.getRole())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能直接管理管理员账号");
        }
        return target;
    }
}
