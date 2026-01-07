package com.hjkj.pregnancy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要登录注解
 * <p>
 * 标记在Controller方法上，表示该接口需要用户登录后才能访问。
 * JWT拦截器会检查此注解，对标记的方法进行token验证。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @RequireLogin
 * @GetMapping("/profile")
 * public Result<UserProfile> getProfile() {
 *     // 方法执行前会先验证token
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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
}


