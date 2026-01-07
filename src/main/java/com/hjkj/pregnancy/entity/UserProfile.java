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
     * 末次月经日期（首次登录可为空，完善档案时必填）
     */
    @Column(name = "last_menstrual_period")
    private LocalDate lastMenstrualPeriod;

    /**
     * 身高(cm)（首次登录可为空，完善档案时必填）
     */
    @Column(name = "height")
    private Integer height;

    /**
     * 当前体重(kg)（首次登录可为空，完善档案时必填）
     */
    @Column(name = "current_weight")
    private Double currentWeight;

    /**
     * 出生日期（首次登录可为空，完善档案时必填）
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * 饮食偏好（可选）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine_preference", length = 30)
    private CuisinePreference cuisinePreference;

    /**
     * 过敏源 (部分逗号分隔)
     */
    @Column(name = "allergies")
    private String allergies;

    /**
     * 忌口 (部分逗号分隔)
     */
    @Column(name = "dietary_restrictions")
    private String dietaryRestrictions;

    /**
     * 饮食强偏好 (部分逗号分隔)
     */
    @Column(name = "preferences")
    private String preferences;

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
