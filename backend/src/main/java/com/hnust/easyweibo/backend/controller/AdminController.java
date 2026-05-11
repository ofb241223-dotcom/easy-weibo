package com.hnust.easyweibo.backend.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hnust.easyweibo.backend.domain.dto.admin.AdminOverviewResponse;
import com.hnust.easyweibo.backend.domain.dto.admin.AdminReportResponse;
import com.hnust.easyweibo.backend.domain.dto.admin.ModerationActionRequest;
import com.hnust.easyweibo.backend.domain.dto.comment.CommentResponse;
import com.hnust.easyweibo.backend.domain.dto.post.PostResponse;
import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;
import com.hnust.easyweibo.backend.service.AdminService;
import com.hnust.easyweibo.backend.service.AuthService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @GetMapping("/overview")
    public AdminOverviewResponse getOverview(@RequestHeader("Authorization") String authorization) {
        authService.requireAdminUserId(authorization);
        return adminService.getOverview();
    }

    @GetMapping("/reports")
    public List<AdminReportResponse> getReports(@RequestHeader("Authorization") String authorization) {
        authService.requireAdminUserId(authorization);
        return adminService.getReports();
    }

    @GetMapping("/users")
    public List<UserResponse> getUsers(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "muteStatus", required = false) String muteStatus
    ) {
        authService.requireAdminUserId(authorization);
        return adminService.getUsers(query, status, muteStatus);
    }

    @GetMapping("/posts")
    public List<PostResponse> getPosts(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "status", required = false) String status
    ) {
        authService.requireAdminUserId(authorization);
        return adminService.getPosts(query, status);
    }

    @GetMapping("/comments")
    public List<CommentResponse> getComments(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(value = "q", required = false) String query
    ) {
        authService.requireAdminUserId(authorization);
        return adminService.getComments(query);
    }

    @GetMapping("/posts/recent")
    public List<PostResponse> getRecentPosts(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(value = "limit", defaultValue = "8") int limit
    ) {
        authService.requireAdminUserId(authorization);
        return adminService.getRecentPosts(limit);
    }

    @PostMapping("/reports/{id}/resolve")
    public Map<String, String> resolveReport(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long adminUserId = authService.requireAdminUserId(authorization);
        adminService.resolveReport(id, adminUserId);
        return Map.of("message", "举报已处理");
    }

    @PostMapping("/users/{id}/ban")
    public Map<String, String> banUser(
        @PathVariable("id") Long id,
        @Valid @RequestBody ModerationActionRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long adminUserId = authService.requireAdminUserId(authorization);
        adminService.banUser(adminUserId, id, request.reason().trim());
        return Map.of("message", "用户已封禁");
    }

    @PostMapping("/users/{id}/unban")
    public Map<String, String> unbanUser(
        @PathVariable("id") Long id,
        @Valid @RequestBody ModerationActionRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long adminUserId = authService.requireAdminUserId(authorization);
        adminService.unbanUser(adminUserId, id, request.reason().trim());
        return Map.of("message", "用户已解封");
    }

    @PostMapping("/users/{id}/mute")
    public Map<String, String> muteUser(
        @PathVariable("id") Long id,
        @Valid @RequestBody ModerationActionRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long adminUserId = authService.requireAdminUserId(authorization);
        adminService.muteUser(adminUserId, id, request.reason().trim());
        return Map.of("message", "用户已禁言");
    }

    @PostMapping("/users/{id}/unmute")
    public Map<String, String> unmuteUser(
        @PathVariable("id") Long id,
        @Valid @RequestBody ModerationActionRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long adminUserId = authService.requireAdminUserId(authorization);
        adminService.unmuteUser(adminUserId, id, request.reason().trim());
        return Map.of("message", "用户已解除禁言");
    }

    @PostMapping("/posts/{id}/withdraw")
    public Map<String, String> withdrawPost(
        @PathVariable("id") Long id,
        @Valid @RequestBody ModerationActionRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long adminUserId = authService.requireAdminUserId(authorization);
        adminService.withdrawPost(id, adminUserId, request.reason().trim());
        return Map.of("message", "帖子已撤回");
    }

    @DeleteMapping("/posts/{id}")
    public Map<String, String> deletePost(
        @PathVariable("id") Long id,
        @Valid @RequestBody ModerationActionRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long adminUserId = authService.requireAdminUserId(authorization);
        adminService.deletePost(id, adminUserId, request.reason().trim());
        return Map.of("message", "帖子已删除");
    }

    @DeleteMapping("/comments/{id}")
    public Map<String, String> deleteComment(
        @PathVariable("id") Long id,
        @Valid @RequestBody ModerationActionRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long adminUserId = authService.requireAdminUserId(authorization);
        adminService.deleteComment(id, adminUserId, request.reason().trim());
        return Map.of("message", "评论已删除");
    }
}
