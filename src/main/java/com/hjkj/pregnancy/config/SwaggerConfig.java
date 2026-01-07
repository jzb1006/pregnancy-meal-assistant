package com.hjkj.pregnancy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
        // 定义安全方案名称
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title("孕妇今天吃啥 API 文档")
                .description("智能孕期饮食助手后端接口文档\n\n" +
                        "**认证说明：**\n" +
                        "1. 首先调用 `/v1/auth/wx/login` 接口登录获取token\n" +
                        "2. 点击右上角 `Authorize` 按钮，输入token（无需加Bearer前缀）\n" +
                        "3. 后续调用需要登录的接口会自动携带token")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Zhibin Jiang")
                    .email("jiangzhibin@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            // 添加安全配置
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("请输入JWT token（无需添加Bearer前缀）")));
    }

    /**
     * 配置 API 分组
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("认证管理")
                .pathsToMatch("/v1/auth/**")
                .build();
    }

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

