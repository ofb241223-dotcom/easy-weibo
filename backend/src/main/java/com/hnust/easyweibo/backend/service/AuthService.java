package com.hnust.easyweibo.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.domain.dto.auth.ChangePasswordRequest;
import com.hnust.easyweibo.backend.domain.dto.auth.LoginRequest;
import com.hnust.easyweibo.backend.domain.dto.auth.LoginResponse;
import com.hnust.easyweibo.backend.domain.dto.auth.RegisterRequest;
import com.hnust.easyweibo.backend.domain.dto.auth.ResetPasswordRequest;
import com.hnust.easyweibo.backend.domain.entity.UserEntity;
import com.hnust.easyweibo.backend.exception.ApiException;
import com.hnust.easyweibo.backend.security.JwtService;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userService.getEntityByUsername(request.username());
        userService.assertCanLogin(user);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        return new LoginResponse(jwtService.generateToken(user.getId()), userService.toResponse(user, user.getId()));
    }

    public LoginResponse register(RegisterRequest request) {
        if (userService.findByUsernameOrNull(request.username()) != null) {
            throw new ApiException(HttpStatus.CONFLICT, "用户名已存在");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.username().trim());
        user.setNickname(request.nickname().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAvatarUrl("https://picsum.photos/seed/" + request.username().trim() + "/200");
        user.setCoverUrl("https://picsum.photos/seed/" + request.username().trim() + "-cover/1200/400");
        user.setBio("这个人很懒，还没有写简介。");
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setMuteStatus("NORMAL");
        user.setCreatedAt(java.time.LocalDateTime.now());
        userService.createUser(user);

        return new LoginResponse(jwtService.generateToken(user.getId()), userService.toResponse(user, user.getId()));
    }

    public void resetPassword(ResetPasswordRequest request) {
        UserEntity user = userService.getEntityByUsername(request.username().trim());
        if (!user.getNickname().equals(request.nickname().trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户名和昵称不匹配");
        }
        userService.updatePassword(user.getId(), passwordEncoder.encode(request.password()));
    }

    public void changePassword(Long currentUserId, ChangePasswordRequest request) {
        UserEntity user = userService.getEntityById(currentUserId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "新密码不能与当前密码相同");
        }
        userService.updatePassword(currentUserId, passwordEncoder.encode(request.newPassword()));
    }

    public Long requireUserId(String authHeader) {
        return jwtService.parseUserId(authHeader);
    }

    public com.hnust.easyweibo.backend.domain.dto.user.UserResponse currentUser(String authHeader) {
        Long userId = requireUserId(authHeader);
        return userService.getById(userId, userId);
    }

    public Long requireAdminUserId(String authHeader) {
        Long userId = requireUserId(authHeader);
        if (!userService.isAdmin(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "没有管理员权限");
        }
        return userId;
    }
}
