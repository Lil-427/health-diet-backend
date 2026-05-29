package com.health.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 工具类
 * 提供 Token 的生成和解析功能
 */
@Component
public class JwtUtils {

    /** 签名算法：HS256（要求密钥至少 32 字节） */
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    private final SecretKey key;
    private final long expiration;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration}") long expiration) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        // Keys.hmacShaKeyFor 会根据密钥长度自动选择算法，
        // 此处显式指定 HS256 确保算法一致
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return Token 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SIGNATURE_ALGORITHM)
                .compact();
    }

    /**
     * 从 Token 中解析用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getBody().getSubject();
    }

    /**
     * 从 Token 中解析用户 ID
     *
     * @param token JWT Token
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return parseToken(token).getBody().get("userId", Long.class);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true 有效 / false 无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析 Token
     */
    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
