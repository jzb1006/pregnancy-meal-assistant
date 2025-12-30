package com.hjkj.pregnancy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * AI请求日志实体
 * 记录所有AI请求的详细信息
 *
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_request_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_scenario", columnList = "scenario"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Comment("AI请求日志表")
public class AiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", length = 100)
    @Comment("用户标识")
    private String userId;

    @Column(name = "scenario", length = 50)
    @Comment("业务场景")
    private String scenario;

    @Column(name = "meal_type", length = 20)
    @Comment("餐次类型")
    private String mealType;

    @Column(name = "prompt_content", columnDefinition = "TEXT")
    @Comment("Prompt内容")
    private String promptContent;

    @Column(name = "prompt_length")
    @Comment("Prompt长度")
    private Integer promptLength;

    @Column(name = "response_content", columnDefinition = "TEXT")
    @Comment("AI响应内容")
    private String responseContent;

    @Column(name = "response_length")
    @Comment("响应长度")
    private Integer responseLength;

    @Column(name = "duration")
    @Comment("耗时(毫秒)")
    private Long duration;

    @Column(name = "is_success")
    @Comment("是否成功")
    private Boolean isSuccess;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "token_usage")
    @Comment("Token使用量")
    private Integer tokenUsage;

    @Column(name = "model_name", length = 50)
    @Comment("模型名称")
    private String modelName;

    @Column(name = "created_at")
    @Comment("创建时间")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (promptLength == null && promptContent != null) {
            promptLength = promptContent.length();
        }
        if (responseLength == null && responseContent != null) {
            responseLength = responseContent.length();
        }
    }
}


