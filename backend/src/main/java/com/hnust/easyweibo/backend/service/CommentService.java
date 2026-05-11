package com.hnust.easyweibo.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import com.hnust.easyweibo.backend.domain.dto.comment.CommentResponse;
import com.hnust.easyweibo.backend.domain.dto.comment.CreateCommentRequest;
import com.hnust.easyweibo.backend.domain.entity.CommentEntity;
import com.hnust.easyweibo.backend.domain.entity.CommentImageEntity;
import com.hnust.easyweibo.backend.exception.ApiException;
import com.hnust.easyweibo.backend.mapper.CommentImageMapper;
import com.hnust.easyweibo.backend.mapper.CommentMapper;
import com.hnust.easyweibo.backend.mapper.PostMapper;

@Service
public class CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9_]+)");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int MAX_COMMENT_IMAGES = 4;

    private final CommentMapper commentMapper;
    private final CommentImageMapper commentImageMapper;
    private final PostMapper postMapper;
    private final UserService userService;
    private final PostService postService;
    private final NotificationService notificationService;

    public CommentService(
        CommentMapper commentMapper,
        CommentImageMapper commentImageMapper,
        PostMapper postMapper,
        UserService userService,
        PostService postService,
        NotificationService notificationService
    ) {
        this.commentMapper = commentMapper;
        this.commentImageMapper = commentImageMapper;
        this.postMapper = postMapper;
        this.userService = userService;
        this.postService = postService;
        this.notificationService = notificationService;
    }

    public List<CommentResponse> getByPostId(Long postId, Long viewerId) {
        postService.getById(postId, viewerId);
        return commentMapper.findByPostId(postId).stream().map(comment -> toResponse(comment, viewerId)).toList();
    }

    public List<CommentResponse> getByAuthorId(Long authorId, Long viewerId) {
        userService.getEntityById(authorId);
        return commentMapper.findByAuthorId(authorId).stream().map(comment -> toResponse(comment, viewerId)).toList();
    }

    public List<CommentResponse> getCommentsForAdmin(String query) {
        return commentMapper.findForAdmin(query).stream()
            .map(comment -> toResponse(comment, null))
            .toList();
    }

    public CommentResponse addComment(Long postId, Long authorId, CreateCommentRequest request) {
        userService.assertCanCreateContent(authorId);
        var post = postMapper.findById(postId);
        postService.getById(postId, authorId);
        String content = request.content() == null ? "" : request.content().trim();
        List<String> imageUrls = request.imageUrls() == null
            ? Collections.emptyList()
            : request.imageUrls().stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .toList();
        if (content.isBlank() && imageUrls.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "回复内容和图片不能同时为空");
        }
        if (content.length() > 500) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "评论内容不能超过 500 个字符");
        }
        if (imageUrls.size() > MAX_COMMENT_IMAGES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "单条回复最多上传 4 张图片");
        }
        CommentEntity comment = new CommentEntity();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(content.isBlank() ? null : content);
        comment.setLikesCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        for (String imageUrl : imageUrls) {
            CommentImageEntity image = new CommentImageEntity();
            image.setCommentId(comment.getId());
            image.setImageUrl(imageUrl);
            commentImageMapper.insert(image);
        }
        postMapper.incrementCommentCount(postId);
        if (post != null) {
            notificationService.createCommentNotification(authorId, post.getAuthorId(), postId);
        }
        notificationService.createMentionNotifications(authorId, extractMentionUserIds(content), postId);
        return toResponse(comment, authorId);
    }

    public void deleteAsAdmin(Long commentId, Long adminUserId, String reason) {
        CommentEntity comment = commentMapper.findById(commentId);
        if (comment == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "评论不存在");
        }
        commentMapper.deleteById(commentId);
        postMapper.decrementCommentCount(comment.getPostId());
        notificationService.createSystemNotification(
            adminUserId,
            comment.getAuthorId(),
            comment.getPostId(),
            "你的评论已被管理员删除。原因：" + reason,
            null,
            null
        );
    }

    private CommentResponse toResponse(CommentEntity comment, Long viewerId) {
        return new CommentResponse(
            String.valueOf(comment.getId()),
            String.valueOf(comment.getPostId()),
            String.valueOf(comment.getAuthorId()),
            userService.getById(comment.getAuthorId(), viewerId),
            comment.getContent() == null ? "" : comment.getContent(),
            commentImageMapper.findByCommentId(comment.getId()).stream()
                .map(CommentImageEntity::getImageUrl)
                .toList(),
            comment.getCreatedAt().format(TIME_FORMATTER),
            comment.getLikesCount(),
            false
        );
    }

    private List<Long> extractMentionUserIds(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyList();
        }
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            var user = userService.findByUsernameOrNull(matcher.group(1));
            if (user != null) {
                userIds.add(user.getId());
            }
        }
        return new ArrayList<>(userIds);
    }
}
