package com.hjkj.pregnancy.utils;

import com.hjkj.pregnancy.exception.AuthException;
import com.hjkj.pregnancy.exception.ErrorCode;
import com.hjkj.pregnancy.interceptor.JwtAuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 认证上下文工具类
 * <p>
 * 提供便捷的方法获取当前登录用户的信息，如openId和userId。
 * 信息从request attribute中获取，由JWT拦截器在验证token后设置。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @RequireLogin
 * @GetMapping("/profile")
 * public Result<UserProfile> getProfile() {
 *     String openId = AuthContext.getCurrentOpenId();
 *     return userService.getProfile(openId);
 * }
 * }
 * </pre>
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Slf4j
public class AuthContext {

    /**
     * 获取当前登录用户的openId
     * <p>
     * 必须在标记了@RequireLogin的接口中调用，否则会抛出异常。
     * </p>
     *
     * @return 当前用户的openId
     * @throws AuthException 如果未登录或未找到openId
     */
    public static String getCurrentOpenId() {
        HttpServletRequest request = getRequest();
        Object openId = request.getAttribute(JwtAuthInterceptor.ATTR_OPEN_ID);
        if (openId == null) {
            log.error("无法获取当前用户openId，请确保接口已添加@RequireLogin注解");
            throw new AuthException(ErrorCode.UNAUTHORIZED);
        }
        return (String) openId;
    }

    /**
     * 获取当前登录用户的userId（数据库主键ID）
     * <p>
     * 必须在标记了@RequireLogin的接口中调用。
     * 如果用户未完成档案注册，可能返回null。
     * </p>
     *
     * @return 当前用户的userId，如果未注册返回null
     * @throws AuthException 如果未登录
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        Object userId = request.getAttribute(JwtAuthInterceptor.ATTR_USER_ID);
        if (userId == null) {
            return null;
        }
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从Spring上下文中获取当前请求的HttpServletRequest
     *
     * @return HttpServletRequest对象
     * @throws AuthException 如果无法获取request（不在请求上下文中）
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("无法获取RequestAttributes，可能不在请求上下文中");
            throw new AuthException(ErrorCode.UNAUTHORIZED);
        }
        return attributes.getRequest();
    }
}


