package com.hnust.easyweibo.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hnust.easyweibo.backend.config.AppProperties;
import com.hnust.easyweibo.backend.domain.dto.upload.UploadResponse;
import com.hnust.easyweibo.backend.exception.ApiException;

@Service
public class UploadService {

    private static final Set<String> CHAT_FILE_TYPES = Set.of(
        "image/png",
        "image/jpeg",
        "image/webp",
        "image/gif",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/zip",
        "application/x-zip-compressed",
        "text/plain"
    );

    private final Path uploadRoot;

    public UploadService(AppProperties appProperties) {
        this.uploadRoot = Paths.get(appProperties.getUploadDir()).toAbsolutePath();
    }

    public UploadResponse uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "只支持图片上传");
        }

        return saveFile(file, contentType, "图片上传失败");
    }

    public UploadResponse uploadChatAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !CHAT_FILE_TYPES.contains(contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "只支持图片和常见文档上传");
        }

        return saveFile(file, contentType, "文件上传失败");
    }

    private UploadResponse saveFile(MultipartFile file, String contentType, String errorMessage) {
        try {
            Files.createDirectories(uploadRoot);
            String originalFilename = file.getOriginalFilename() == null
                ? "file"
                : Paths.get(file.getOriginalFilename()).getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
            String filename = UUID.randomUUID() + "-" + originalFilename;
            Path target = uploadRoot.resolve(filename);
            file.transferTo(target);
            return new UploadResponse("/uploads/" + filename, originalFilename, contentType);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }
}
