package com.hjkj.pregnancy.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.config.JwtProperties;
import com.hjkj.pregnancy.utils.JwtUtil;
import com.hjkj.pregnancy.utils.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT认证拦截器
 * <p>
 * 拦截所有标记了@RequireLogin注解的接口，验证JWT token的有效性。
 * 如果token有效，将用户的openId和userId放入request attribute中，
 * 供后续业务代码使用。
 * </p>
 * <p>
 * 处理流程：
 * <ol>
 *   <li>检查方法是否标记了@RequireLogin注解</li>
 *   <li>从请求头Authorization中提取token</li>
 *   <li>验证token的有效性和签名</li>
 *   <li>解析token获取openId和userId</li>
 *   <li>将用户信息存入request attribute</li>
 * </ol>
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    /**
     * request attribute中存储openId的键名
     */
    public static final String ATTR_OPEN_ID = "openId";

    /**
     * request attribute中存储userId的键名
     */
    public static final String ATTR_USER_ID = "userId";

    /**
     * 前置拦截方法，在Controller方法执行前调用
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return true表示继续执行，false表示拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查方法是否有@RequireLogin注解
        RequireLogin requireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);
        if (requireLogin == null) {
            // 没有注解，不需要验证，直接放行
            return true;
        }

        // 需要登录验证
        try {
            // 1. 从请求头获取token
            String token = extractToken(request);
            if (!StringUtils.hasText(token)) {
                log.warn("请求需要登录但未提供token: {} {}", request.getMethod(), request.getRequestURI());
                return unauthorized(response, "未登录或token已过期");
            }

            // 2. 验证token有效性
            if (!jwtUtil.validateToken(token)) {
                log.warn("token验证失败: {}", token);
                return unauthorized(response, "token无效");
            }

            // 3. 解析token获取用户信息
            String openId = jwtUtil.getOpenIdFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            if (!StringUtils.hasText(openId)) {
                log.error("token中未包含openId: {}", token);
                return unauthorized(response, "token格式错误");
            }

            // 4. 将用户信息存入request attribute
            request.setAttribute(ATTR_OPEN_ID, openId);
            if (userId != null) {
                request.setAttribute(ATTR_USER_ID, userId);
            }

            log.debug("JWT验证成功: openId={}, userId={}", openId, userId);
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("token已过期: {}", e.getMessage());
            return unauthorized(response, "token已过期，请重新登录");
        } catch (JwtException e) {
            log.error("token解析失败: {}", e.getMessage());
            return unauthorized(response, "token无效");
        } catch (Exception e) {
            log.error("JWT验证时发生异常", e);
            return unauthorized(response, "认证失败");
        }
    }

    /**
     * 从请求头中提取JWT token
     *
     * @param request HTTP请求
     * @return token字符串，如果不存在返回null
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.hasText(header) && header.startsWith(jwtProperties.getTokenPrefix())) {
            return header.substring(jwtProperties.getTokenPrefix().length());
        }
        return null;
    }

    /**
     * 返回401未授权响应
     *
     * @param response HTTP响应
     * @param message  错误消息
     * @return false，表示拦截请求
     */
    private boolean unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Result<Object> result = Result.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
}

