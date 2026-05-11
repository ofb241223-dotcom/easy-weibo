package com.hnust.easyweibo.backend.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hnust.easyweibo.backend.domain.dto.post.CreatePostRequest;
import com.hnust.easyweibo.backend.domain.dto.post.PostResponse;
import com.hnust.easyweibo.backend.domain.dto.post.PostViewRecordResponse;
import com.hnust.easyweibo.backend.domain.dto.post.RepublishPostRequest;
import com.hnust.easyweibo.backend.domain.dto.post.ReportPostRequest;
import com.hnust.easyweibo.backend.domain.dto.post.UpdatePostRequest;
import com.hnust.easyweibo.backend.service.AuthService;
import com.hnust.easyweibo.backend.service.PostService;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @GetMapping("/posts")
    public List<PostResponse> listPosts(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return postService.getAll(viewerId);
    }

    @GetMapping("/posts/{id}")
    public PostResponse getPost(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return postService.getDetailById(id, viewerId);
    }

    @GetMapping("/posts/{id}/views")
    public List<PostViewRecordResponse> getPostViews(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return postService.getViewRecords(id, currentUserId);
    }

    @PostMapping("/posts")
    public PostResponse createPost(
        @Valid @RequestBody CreatePostRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return postService.create(currentUserId, request);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/posts/{id}")
    public PostResponse updatePost(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdatePostRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return postService.update(id, currentUserId, request);
    }

    @PostMapping("/posts/{id}/republish")
    public PostResponse republishPost(
        @PathVariable("id") Long id,
        @Valid @RequestBody RepublishPostRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return postService.republish(id, currentUserId, request);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/posts/{id}")
    public java.util.Map<String, String> deletePost(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        postService.delete(id, currentUserId);
        return java.util.Map.of("message", "帖子已删除");
    }

    @PostMapping("/posts/{id}/like")
    public PostResponse toggleLike(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return postService.toggleLike(id, currentUserId);
    }

    @PostMapping("/posts/{id}/bookmark")
    public PostResponse toggleBookmark(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return postService.toggleBookmark(id, currentUserId);
    }

    @PostMapping("/posts/{id}/repost")
    public PostResponse toggleRepost(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return postService.toggleRepost(id, currentUserId);
    }

    @PostMapping("/posts/{id}/hide")
    public java.util.Map<String, String> hidePost(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        postService.hide(id, currentUserId);
        return java.util.Map.of("message", "已隐藏这条帖子");
    }

    @PostMapping("/posts/{id}/report")
    public java.util.Map<String, String> reportPost(
        @PathVariable("id") Long id,
        @Valid @RequestBody ReportPostRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        postService.report(id, currentUserId, request);
        return java.util.Map.of("message", "举报已提交，管理员会尽快处理");
    }

    @GetMapping("/users/{id}/posts")
    public List<PostResponse> getPostsByAuthor(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return postService.getByAuthor(id, viewerId);
    }

    @GetMapping("/users/{id}/likes")
    public List<PostResponse> getLikedPostsByUser(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return postService.getLikedByUser(id, viewerId);
    }

    @GetMapping("/users/{id}/bookmarks")
    public List<PostResponse> getBookmarkedPostsByUser(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return postService.getBookmarkedByUser(id, viewerId);
    }

    @GetMapping("/users/{id}/reposts")
    public List<PostResponse> getRepostedPostsByUser(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return postService.getRepostedByUser(id, viewerId);
    }

    @GetMapping("/posts/search")
    public List<PostResponse> searchPosts(
        @RequestParam("q") String query,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return postService.search(query, viewerId);
    }

    @GetMapping("/posts/tag/{tag}")
    public List<PostResponse> getPostsByTag(
        @PathVariable("tag") String tag,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return postService.getByTag(tag, viewerId);
    }
}
