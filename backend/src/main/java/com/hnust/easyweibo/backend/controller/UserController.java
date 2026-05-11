package com.hnust.easyweibo.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.hnust.easyweibo.backend.domain.dto.comment.CommentResponse;
import com.hnust.easyweibo.backend.domain.dto.auth.ChangePasswordRequest;
import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;
import com.hnust.easyweibo.backend.domain.dto.user.UpdateProfileRequest;
import com.hnust.easyweibo.backend.service.AuthService;
import com.hnust.easyweibo.backend.service.CommentService;
import com.hnust.easyweibo.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final CommentService commentService;

    public UserController(UserService userService, AuthService authService, CommentService commentService) {
        this.userService = userService;
        this.authService = authService;
        this.commentService = commentService;
    }

    @GetMapping("/search")
    public java.util.List<UserResponse> searchUsers(
        @RequestParam("q") String query,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return userService.search(query, viewerId);
    }

    @GetMapping("/recommended")
    public java.util.List<UserResponse> recommendedUsers(
        @RequestParam(value = "limit", defaultValue = "5") int limit,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return userService.getRecommendedUsers(viewerId, limit);
    }

    @GetMapping("/{username}")
    public UserResponse getByUsername(
        @PathVariable("username") String username,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return userService.getByUsername(username, viewerId);
    }

    @GetMapping("/{id}/comments")
    public java.util.List<CommentResponse> listCommentsByAuthor(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return commentService.getByAuthorId(id, viewerId);
    }

    @GetMapping("/{id}/following")
    public java.util.List<UserResponse> listFollowing(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return userService.getFollowingUsers(id, viewerId);
    }

    @GetMapping("/{id}/followers")
    public java.util.List<UserResponse> listFollowers(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return userService.getFollowerUsers(id, viewerId);
    }

    @GetMapping("/{id}/mutuals")
    public java.util.List<UserResponse> listMutuals(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return userService.getMutualUsers(id, viewerId);
    }

    @PatchMapping("/me")
    public UserResponse updateCurrentUser(
        @Valid @RequestBody UpdateProfileRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return userService.updateCurrentUser(currentUserId, request);
    }

    @PatchMapping("/me/password")
    public Map<String, String> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        authService.changePassword(currentUserId, request);
        return Map.of("message", "密码修改成功");
    }

    @PostMapping("/{id}/follow")
    public Map<String, String> toggleFollow(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        userService.toggleFollow(currentUserId, id);
        return Map.of("message", "操作成功");
    }
}
