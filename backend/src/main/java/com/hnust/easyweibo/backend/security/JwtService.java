package com.hnust.easyweibo.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.config.AppProperties;
import com.hnust.easyweibo.backend.exception.ApiException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expireHours;

    public JwtService(AppProperties appProperties) {
        this.key = Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        this.expireHours = appProperties.getJwt().getExpireHours();
    }

    public String generateToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expireHours, ChronoUnit.HOURS)))
            .signWith(key)
            .compact();
    }

    public Long parseUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "请先登录");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "登录状态无效，请重新登录");
        }
    }
}
