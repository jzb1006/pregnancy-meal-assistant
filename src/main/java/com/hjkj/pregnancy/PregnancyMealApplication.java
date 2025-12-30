package com.hjkj.pregnancy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 孕妇今天吃啥 - 主启动类
 * 
 * @author Zhibin Jiang
 * @version 1.0.0
 */
@SpringBootApplication
public class PregnancyMealApplication {

    public static void main(String[] args) {
        SpringApplication.run(PregnancyMealApplication.class, args);
        System.out.println("""
            
            ========================================
            🍽️  孕妇今天吃啥 启动成功！
            📖  Swagger UI: http://localhost:8080/api/swagger-ui.html
            📄  API Docs:   http://localhost:8080/api/v3/api-docs
            ========================================
            """);
    }
}

