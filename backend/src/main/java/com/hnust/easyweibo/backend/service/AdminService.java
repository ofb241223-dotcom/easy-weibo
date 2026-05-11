package com.hnust.easyweibo.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.domain.dto.admin.AdminOverviewResponse;
import com.hnust.easyweibo.backend.domain.dto.admin.AdminReportPostResponse;
import com.hnust.easyweibo.backend.domain.dto.admin.AdminReportResponse;
import com.hnust.easyweibo.backend.domain.dto.comment.CommentResponse;
import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;
import com.hnust.easyweibo.backend.domain.entity.ReportEntity;
import com.hnust.easyweibo.backend.exception.ApiException;
import com.hnust.easyweibo.backend.mapper.CommentMapper;
import com.hnust.easyweibo.backend.mapper.PostMapper;
import com.hnust.easyweibo.backend.mapper.ReportMapper;

@Service
public class AdminService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UserService userService;
    private final PostService postService;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final CommentService commentService;

    public AdminService(
        UserService userService,
        PostService postService,
        PostMapper postMapper,
        CommentMapper commentMapper,
        ReportMapper reportMapper,
        CommentService commentService
    ) {
        this.userService = userService;
        this.postService = postService;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.reportMapper = reportMapper;
        this.commentService = commentService;
    }

    public AdminOverviewResponse getOverview() {
        return new AdminOverviewResponse(
            userService.countUsers(),
            postService.countPosts(),
            commentMapper.countAll(),
            reportMapper.countOpen(),
            postService.countTotalViews()
        );
    }

    public List<AdminReportResponse> getReports() {
        return reportMapper.findAll().stream()
            .map(this::toReportResponse)
            .toList();
    }

    public List<com.hnust.easyweibo.backend.domain.dto.post.PostResponse> getRecentPosts(int limit) {
        return postService.getRecentPosts(limit);
    }

    public List<UserResponse> getUsers(String query, String status, String muteStatus) {
        return userService.getUsersForAdmin(query, status, muteStatus);
    }

    public List<com.hnust.easyweibo.backend.domain.dto.post.PostResponse> getPosts(String query, String status) {
        return postService.getPostsForAdmin(query, status);
    }

    public List<CommentResponse> getComments(String query) {
        return commentService.getCommentsForAdmin(query);
    }

    public void resolveReport(Long reportId, Long adminUserId) {
        ReportEntity report = reportMapper.findById(reportId);
        if (report == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "举报不存在");
        }
        reportMapper.resolve(reportId, adminUserId, java.time.LocalDateTime.now());
    }

    public void deletePost(Long postId, Long adminUserId) {
        postService.deleteAsAdmin(postId, adminUserId, "违反社区规范");
    }

    public void deletePost(Long postId, Long adminUserId, String reason) {
        postService.deleteAsAdmin(postId, adminUserId, reason);
    }

    public void withdrawPost(Long postId, Long adminUserId, String reason) {
        postService.withdrawAsAdmin(postId, adminUserId, reason);
    }

    public void deleteComment(Long commentId, Long adminUserId, String reason) {
        commentService.deleteAsAdmin(commentId, adminUserId, reason);
    }

    public void banUser(Long adminUserId, Long targetUserId, String reason) {
        userService.banUser(adminUserId, targetUserId, reason);
    }

    public void unbanUser(Long adminUserId, Long targetUserId, String reason) {
        userService.unbanUser(adminUserId, targetUserId, reason);
    }

    public void muteUser(Long adminUserId, Long targetUserId, String reason) {
        userService.muteUser(adminUserId, targetUserId, reason);
    }

    public void unmuteUser(Long adminUserId, Long targetUserId, String reason) {
        userService.unmuteUser(adminUserId, targetUserId, reason);
    }

    private AdminReportResponse toReportResponse(ReportEntity report) {
        var post = postMapper.findById(report.getPostId());
        var reporter = userService.getById(report.getReporterId(), report.getReporterId());
        var resolvedBy = report.getResolvedBy() == null ? null : userService.getById(report.getResolvedBy(), report.getResolvedBy());

        AdminReportPostResponse postResponse = post == null
            ? null
            : new AdminReportPostResponse(
                String.valueOf(post.getId()),
                post.getContent(),
                post.getStatus(),
                post.getCreatedAt().format(TIME_FORMATTER),
                userService.getById(post.getAuthorId(), post.getAuthorId())
            );

        return new AdminReportResponse(
            String.valueOf(report.getId()),
            report.getCategory(),
            report.getDetails(),
            report.getStatus(),
            report.getCreatedAt().format(TIME_FORMATTER),
            report.getResolvedAt() == null ? null : report.getResolvedAt().format(TIME_FORMATTER),
            reporter,
            resolvedBy,
            postResponse
        );
    }
}
