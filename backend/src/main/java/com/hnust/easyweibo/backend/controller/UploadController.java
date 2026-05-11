package com.hnust.easyweibo.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hnust.easyweibo.backend.domain.dto.upload.UploadResponse;
import com.hnust.easyweibo.backend.service.AuthService;
import com.hnust.easyweibo.backend.service.UploadService;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;
    private final AuthService authService;

    public UploadController(UploadService uploadService, AuthService authService) {
        this.uploadService = uploadService;
        this.authService = authService;
    }

    @PostMapping("/images")
    public UploadResponse uploadImage(
        @RequestParam("file") MultipartFile file,
        @RequestHeader("Authorization") String authorization
    ) {
        authService.requireUserId(authorization);
        return uploadService.uploadImage(file);
    }

    @PostMapping("/files")
    public UploadResponse uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestHeader("Authorization") String authorization
    ) {
        authService.requireUserId(authorization);
        return uploadService.uploadChatAttachment(file);
    }
}
