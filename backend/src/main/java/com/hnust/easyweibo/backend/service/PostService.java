package com.hnust.easyweibo.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.domain.dto.post.CreatePostRequest;
import com.hnust.easyweibo.backend.domain.dto.post.PostResponse;
import com.hnust.easyweibo.backend.domain.dto.post.PostViewRecordResponse;
import com.hnust.easyweibo.backend.domain.dto.post.RepublishPostRequest;
import com.hnust.easyweibo.backend.domain.dto.post.ReportPostRequest;
import com.hnust.easyweibo.backend.domain.dto.post.UpdatePostRequest;
import com.hnust.easyweibo.backend.domain.entity.PostEntity;
import com.hnust.easyweibo.backend.domain.entity.PostImageEntity;
import com.hnust.easyweibo.backend.domain.entity.PostViewEntity;
import com.hnust.easyweibo.backend.domain.entity.ReportEntity;
import com.hnust.easyweibo.backend.exception.ApiException;
import com.hnust.easyweibo.backend.mapper.PostImageMapper;
import com.hnust.easyweibo.backend.mapper.PostMapper;
import com.hnust.easyweibo.backend.mapper.PostViewMapper;
import com.hnust.easyweibo.backend.mapper.ReportMapper;

@Service
public class PostService {

    private static final Pattern TAG_PATTERN = Pattern.compile("#([^\\s#]+)");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9_]+)");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final PostViewMapper postViewMapper;
    private final ReportMapper reportMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    public PostService(
        PostMapper postMapper,
        PostImageMapper postImageMapper,
        PostViewMapper postViewMapper,
        ReportMapper reportMapper,
        UserService userService,
        NotificationService notificationService
    ) {
        this.postMapper = postMapper;
        this.postImageMapper = postImageMapper;
        this.postViewMapper = postViewMapper;
        this.reportMapper = reportMapper;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public List<PostResponse> getAll(Long viewerId) {
        return filterVisiblePosts(postMapper.findAll(), viewerId).stream().map(post -> toResponse(post, viewerId)).toList();
    }

    public PostResponse getById(Long id, Long viewerId) {
        PostEntity post = requireVisiblePost(id, viewerId);
        return toResponse(post, viewerId);
    }

    public PostResponse getDetailById(Long id, Long viewerId) {
        PostEntity post = requireVisiblePost(id, viewerId);
        if (viewerId != null) {
            PostViewEntity view = new PostViewEntity();
            view.setPostId(id);
            view.setViewerId(viewerId);
            view.setViewedAt(java.time.LocalDateTime.now());
            postViewMapper.insert(view);
            postMapper.incrementViewCount(id);
        }
        return toResponse(postMapper.findById(id), viewerId);
    }

    public List<PostViewRecordResponse> getViewRecords(Long postId, Long requesterId) {
        PostEntity post = postMapper.findById(postId);
        if (post == null || !isVisibleToViewer(post, requesterId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        if (!post.getAuthorId().equals(requesterId) && !userService.isAdmin(requesterId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只有作者和管理员可以查看浏览记录");
        }
        return postViewMapper.findByPostId(postId).stream()
            .map(view -> new PostViewRecordResponse(
                String.valueOf(view.getId()),
                userService.getById(view.getViewerId(), requesterId),
                view.getViewedAt().format(TIME_FORMATTER)
            ))
            .toList();
    }

    public List<PostResponse> getByAuthor(Long authorId, Long viewerId) {
        return filterVisiblePosts(postMapper.findByAuthorId(authorId), viewerId).stream().map(post -> toResponse(post, viewerId)).toList();
    }

    public List<PostResponse> getLikedByUser(Long userId, Long viewerId) {
        return filterVisiblePosts(postMapper.findLikedByUserId(userId), viewerId).stream().map(post -> toResponse(post, viewerId)).toList();
    }

    public List<PostResponse> getBookmarkedByUser(Long userId, Long viewerId) {
        return filterVisiblePosts(postMapper.findBookmarkedByUserId(userId), viewerId).stream().map(post -> toResponse(post, viewerId)).toList();
    }

    public List<PostResponse> getRepostedByUser(Long userId, Long viewerId) {
        return filterVisiblePosts(postMapper.findRepostedByUserId(userId), viewerId).stream().map(post -> toResponse(post, viewerId)).toList();
    }

    public List<PostResponse> search(String query, Long viewerId) {
        return filterVisiblePosts(postMapper.search(query), viewerId).stream().map(post -> toResponse(post, viewerId)).toList();
    }

    public List<PostResponse> getByTag(String tag, Long viewerId) {
        return filterVisiblePosts(postMapper.findByTag(tag), viewerId).stream().map(post -> toResponse(post, viewerId)).toList();
    }

    public PostResponse create(Long authorId, CreatePostRequest request) {
        userService.assertCanCreateContent(authorId);
        String content = request.content().trim();
        PostEntity post = new PostEntity();
        post.setAuthorId(authorId);
        post.setContent(content);
        post.setStatus("ACTIVE");
        post.setLikesCount(0);
        post.setRepostsCount(0);
        post.setCommentsCount(0);
        post.setViewsCount(0);
        post.setCreatedAt(java.time.LocalDateTime.now());
        postMapper.insert(post);

        if (request.imageUrls() != null) {
            for (String imageUrl : request.imageUrls()) {
                if (imageUrl == null || imageUrl.isBlank()) {
                    continue;
                }
                PostImageEntity image = new PostImageEntity();
                image.setPostId(post.getId());
                image.setImageUrl(imageUrl);
                postImageMapper.insert(image);
            }
        }

        notificationService.createMentionNotifications(authorId, extractMentionUserIds(content), post.getId());

        return getById(post.getId(), authorId);
    }

    public PostResponse update(Long postId, Long currentUserId, UpdatePostRequest request) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只能编辑自己的帖子");
        }
        if ("DELETED".equalsIgnoreCase(post.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "已删除的帖子不能再编辑");
        }

        postMapper.updateContent(postId, request.content().trim());
        return getById(postId, currentUserId);
    }

    public PostResponse republish(Long postId, Long currentUserId, RepublishPostRequest request) {
        userService.assertCanCreateContent(currentUserId);
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只能重新发布自己的帖子");
        }
        if (!"WITHDRAWN".equalsIgnoreCase(post.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "只有已撤回的帖子可以重新发布");
        }

        postMapper.republish(postId, request.content().trim(), "ACTIVE");
        return getById(postId, currentUserId);
    }

    public void delete(Long postId, Long currentUserId) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "只能删除自己的帖子");
        }

        postMapper.deleteById(postId);
    }

    public void deleteAsAdmin(Long postId, Long adminUserId) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }

        reportMapper.resolveByPost(postId, adminUserId, java.time.LocalDateTime.now());
        postMapper.updateStatus(postId, "DELETED");
    }

    public void withdrawAsAdmin(Long postId, Long adminUserId, String reason) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        reportMapper.resolveByPost(postId, adminUserId, java.time.LocalDateTime.now());
        postMapper.updateStatus(postId, "WITHDRAWN");
        notificationService.createSystemNotification(
            adminUserId,
            post.getAuthorId(),
            postId,
            "你的帖子已被管理员撤回。原因：" + reason,
            "重新编辑",
            "/compose?edit=" + postId
        );
    }

    public void deleteAsAdmin(Long postId, Long adminUserId, String reason) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }

        reportMapper.resolveByPost(postId, adminUserId, java.time.LocalDateTime.now());
        postMapper.updateStatus(postId, "DELETED");
        notificationService.createSystemNotification(
            adminUserId,
            post.getAuthorId(),
            postId,
            "你的帖子已被管理员删除。原因：" + reason,
            null,
            null
        );
    }

    public PostResponse toggleLike(Long postId, Long currentUserId) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }

        boolean liked = postMapper.existsLike(postId, currentUserId);
        if (liked) {
            postMapper.deleteLike(postId, currentUserId);
            postMapper.decrementLikeCount(postId);
            notificationService.removeLikeNotification(currentUserId, post.getAuthorId(), postId);
        } else {
            postMapper.insertLike(postId, currentUserId);
            postMapper.incrementLikeCount(postId);
            notificationService.createLikeNotification(currentUserId, post.getAuthorId(), postId);
        }

        return getById(postId, currentUserId);
    }

    public PostResponse toggleBookmark(Long postId, Long currentUserId) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }

        if (postMapper.existsBookmark(postId, currentUserId)) {
            postMapper.deleteBookmark(postId, currentUserId);
        } else {
            postMapper.insertBookmark(postId, currentUserId);
        }

        return getById(postId, currentUserId);
    }

    public PostResponse toggleRepost(Long postId, Long currentUserId) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        if (post.getAuthorId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能转发自己的帖子");
        }
        userService.assertCanCreateContent(currentUserId);

        if (postMapper.existsRepost(postId, currentUserId)) {
            postMapper.deleteRepost(postId, currentUserId);
            postMapper.decrementRepostCount(postId);
        } else {
            postMapper.insertRepost(postId, currentUserId);
            postMapper.incrementRepostCount(postId);
        }

        return getById(postId, currentUserId);
    }

    public void hide(Long postId, Long currentUserId) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        if (post.getAuthorId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能隐藏自己的帖子");
        }
        if (postMapper.existsHidden(postId, currentUserId)) {
            throw new ApiException(HttpStatus.CONFLICT, "你已经隐藏过这条帖子");
        }
        postMapper.insertHidden(postId, currentUserId);
    }

    public void report(Long postId, Long currentUserId, ReportPostRequest request) {
        PostEntity post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        if (post.getAuthorId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能举报自己的帖子");
        }
        if (reportMapper.existsOpenByReporter(postId, currentUserId)) {
            throw new ApiException(HttpStatus.CONFLICT, "你已经举报过这条帖子，等待管理员处理");
        }

        ReportEntity report = new ReportEntity();
        report.setPostId(postId);
        report.setReporterId(currentUserId);
        report.setCategory(request.category());
        report.setDetails(request.details() == null ? null : request.details().trim());
        report.setStatus("OPEN");
        report.setCreatedAt(java.time.LocalDateTime.now());
        reportMapper.insert(report);
    }

    public int countPosts() {
        return postMapper.countAll();
    }

    public long countTotalViews() {
        return postMapper.countTotalViews();
    }

    public List<PostResponse> getRecentPosts(int limit) {
        return postMapper.findRecent(limit).stream().map(post -> toResponse(post, null)).toList();
    }

    public List<PostResponse> getPostsForAdmin(String query, String status) {
        return postMapper.findForAdmin(query, status).stream()
            .map(post -> toResponse(post, null))
            .toList();
    }

    public PostResponse toResponse(PostEntity post, Long viewerId) {
        List<String> images = postImageMapper.findByPostId(post.getId()).stream()
            .map(PostImageEntity::getImageUrl)
            .toList();

        boolean isLiked = viewerId != null && postMapper.existsLike(post.getId(), viewerId);
        boolean isReposted = viewerId != null && postMapper.existsRepost(post.getId(), viewerId);
        boolean isBookmarked = viewerId != null && postMapper.existsBookmark(post.getId(), viewerId);

        return new PostResponse(
            String.valueOf(post.getId()),
            String.valueOf(post.getAuthorId()),
            userService.getById(post.getAuthorId(), viewerId),
            post.getContent(),
            images,
            post.getStatus(),
            post.getCreatedAt().format(TIME_FORMATTER),
            post.getLikesCount(),
            post.getRepostsCount(),
            post.getCommentsCount(),
            post.getViewsCount(),
            isLiked,
            isReposted,
            isBookmarked,
            extractTags(post.getContent())
        );
    }

    private PostEntity requireVisiblePost(Long id, Long viewerId) {
        PostEntity post = postMapper.findById(id);
        if (post == null || !isVisibleToViewer(post, viewerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private List<String> extractTags(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyList();
        }
        List<String> tags = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(content);
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }
        return tags;
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

    private List<PostEntity> filterVisiblePosts(List<PostEntity> posts, Long viewerId) {
        return posts.stream()
            .filter(post -> isVisibleToViewer(post, viewerId))
            .toList();
    }

    private boolean isVisibleToViewer(PostEntity post, Long viewerId) {
        if ("DELETED".equalsIgnoreCase(post.getStatus())) {
            return false;
        }
        boolean isAdminViewer = viewerId != null && userService.isAdmin(viewerId);
        if ("WITHDRAWN".equalsIgnoreCase(post.getStatus())) {
            if (viewerId == null) {
                return false;
            }
            if (!post.getAuthorId().equals(viewerId) && !isAdminViewer) {
                return false;
            }
        }

        return viewerId == null || !postMapper.existsHidden(post.getId(), viewerId);
    }
}
