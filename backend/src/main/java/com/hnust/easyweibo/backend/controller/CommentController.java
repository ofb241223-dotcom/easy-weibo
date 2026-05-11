package com.hnust.easyweibo.backend.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hnust.easyweibo.backend.domain.dto.comment.CommentResponse;
import com.hnust.easyweibo.backend.domain.dto.comment.CreateCommentRequest;
import com.hnust.easyweibo.backend.service.AuthService;
import com.hnust.easyweibo.backend.service.CommentService;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    public CommentController(CommentService commentService, AuthService authService) {
        this.commentService = commentService;
        this.authService = authService;
    }

    @GetMapping
    public List<CommentResponse> listComments(
        @PathVariable("postId") Long postId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long viewerId = authorization == null ? null : authService.requireUserId(authorization);
        return commentService.getByPostId(postId, viewerId);
    }

    @PostMapping
    public CommentResponse createComment(
        @PathVariable("postId") Long postId,
        @Valid @RequestBody CreateCommentRequest request,
        @RequestHeader("Authorization") String authorization
    ) {
        Long currentUserId = authService.requireUserId(authorization);
        return commentService.addComment(postId, currentUserId, request);
    }
}
