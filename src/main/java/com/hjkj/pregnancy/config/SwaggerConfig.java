package com.hjkj.pregnancy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置（原生 Swagger）
 * 
 * @author Zhibin Jiang
 */
@Configuration
public class SwaggerConfig {

    /**
     * 自定义 OpenAPI 信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("孕妇今天吃啥 API 文档")
                .description("智能孕期饮食助手后端接口文档")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Zhibin Jiang")
                    .email("jiangzhibin@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

    /**
     * 配置 API 分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .pathsToMatch("/v1/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi mealApi() {
        return GroupedOpenApi.builder()
                .group("食谱推荐")
                .pathsToMatch("/v1/meal/**")
                .build();
    }
}

