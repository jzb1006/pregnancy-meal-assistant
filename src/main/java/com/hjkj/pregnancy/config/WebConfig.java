package com.hjkj.pregnancy.config;

import com.hjkj.pregnancy.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 * 
 * @author Zhibin Jiang
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.beans.factory.annotation.Qualifier("aiStreamExecutor")
    private org.springframework.core.task.AsyncTaskExecutor aiStreamExecutor;

    @Override
    public void configureAsyncSupport(
            org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(aiStreamExecutor);
        configurer.setDefaultTimeout(60000); // 60 seconds timeout
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/v1/**")  // 拦截所有API路径
                .excludePathPatterns(
                        "/v1/auth/**",              // 排除认证接口
                        "/swagger-ui.html",         // 排除Swagger UI
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/error"
                );
    }

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}



