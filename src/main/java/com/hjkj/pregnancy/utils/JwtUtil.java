package com.hjkj.pregnancy.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * <p>
 * 提供JWT token的生成、解析和验证功能。使用HMAC-SHA256算法进行签名，
 * token包含用户的openId和userId信息，支持过期时间配置。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>生成JWT token - 支持仅openId或包含userId</li>
 *   <li>解析和验证token - 检查签名和过期时间</li>
 *   <li>提取token中的用户信息 - openId、userId</li>
 * </ul>
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥，从配置文件读取
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT过期时间（毫秒），从配置文件读取，默认7天
     */
    @Value("${jwt.expiration:604800000}")
    private Long expiration;

    /**
     * Claims中的openId键名
     */
    private static final String CLAIM_KEY_OPEN_ID = "openId";

    /**
     * Claims中的userId键名
     */
    private static final String CLAIM_KEY_USER_ID = "userId";

    /**
     * 生成JWT token（仅包含openId，用于未注册用户）
     *
     * @param openId 微信openId
     * @return JWT token字符串
     */
    public String generateToken(String openId) {
        return generateToken(openId, null);
    }

    /**
     * 生成JWT token（包含openId和userId，用于已注册用户）
     *
     * @param openId 微信openId
     * @param userId 用户数据库ID
     * @return JWT token字符串
     */
    public String generateToken(String openId, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_OPEN_ID, openId);
        if (userId != null) {
            claims.put(CLAIM_KEY_USER_ID, userId);
        }
        return generateToken(claims);
    }

    /**
     * 根据Claims生成JWT token
     *
     * @param claims JWT载荷数据
     * @return JWT token字符串
     */
    private String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析JWT token获取Claims
     *
     * @param token JWT token字符串
     * @return Claims对象，包含token中的所有信息
     * @throws JwtException 如果token无效、过期或签名错误
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("JWT token解析失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 验证JWT token是否有效
     *
     * @param token JWT token字符串
     * @return true表示token有效，false表示无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 从token中获取openId
     *
     * @param token JWT token字符串
     * @return openId，如果不存在返回null
     */
    public String getOpenIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_KEY_OPEN_ID, String.class);
    }

    /**
     * 从token中获取userId
     *
     * @param token JWT token字符串
     * @return userId，如果不存在返回null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get(CLAIM_KEY_USER_ID);
        if (userId == null) {
            return null;
        }
        // 处理Integer到Long的转换
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 判断token是否已过期
     *
     * @param token JWT token字符串
     * @return true表示已过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            log.error("检查token过期状态时出错: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey对象
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}


