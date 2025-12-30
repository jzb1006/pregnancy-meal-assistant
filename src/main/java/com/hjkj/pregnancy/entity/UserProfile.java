package com.hjkj.pregnancy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户档案实体类
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户唯一标识（微信OpenID）
     */
    @Column(name = "open_id", nullable = false, unique = true, length = 64)
    private String openId;

    /**
     * 末次月经日期
     */
    @Column(name = "last_menstrual_period", nullable = false)
    private LocalDate lastMenstrualPeriod;

    /**
     * 身高(cm)
     */
    @Column(name = "height", nullable = false)
    private Integer height;

    /**
     * 当前体重(kg)
     */
    @Column(name = "current_weight", nullable = false)
    private Double currentWeight;

    /**
     * 出生日期
     */
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

