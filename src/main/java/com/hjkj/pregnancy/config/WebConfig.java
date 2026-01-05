package com.hjkj.pregnancy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 * 
 * @author Zhibin Jiang
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

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

