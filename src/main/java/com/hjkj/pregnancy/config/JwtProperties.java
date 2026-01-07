package com.hjkj.pregnancy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性类
 * <p>
 * 从application.yml中读取jwt配置项，包括密钥、过期时间、请求头名称等。
 * 使用@ConfigurationProperties注解自动绑定配置值。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥，至少32个字符
     */
    private String secret;

    /**
     * JWT过期时间（毫秒），默认7天
     */
    private Long expiration = 604800000L;

    /**
     * 请求头名称，默认为Authorization
     */
    private String header = "Authorization";

    /**
     * Token前缀，默认为"Bearer "
     */
    private String tokenPrefix = "Bearer ";
}


