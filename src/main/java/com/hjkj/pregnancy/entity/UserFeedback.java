package com.hjkj.pregnancy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.hjkj.pregnancy.enums.FeedbackAction;

import java.time.LocalDateTime;

/**
 * 用户反馈实体类
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_feedback", indexes = {
        @Index(name = "idx_user_recipe", columnList = "user_id, recipe_id")
})
public class UserFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    /**
     * 反馈动作: LIKE, DISLIKE, BORED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private FeedbackAction action;

    /**
     * 反馈原因（可选）
     */
    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
