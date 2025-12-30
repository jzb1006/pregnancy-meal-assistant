package com.hjkj.pregnancy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 智能食谱实体类
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 菜品名称
     */
    @Column(name = "dish_name", nullable = false, length = 100)
    private String dishName;

    /**
     * 标签（逗号分隔）
     */
    @Column(name = "tags")
    private String tags;

    /**
     * 适用BMI策略
     */
    @Column(name = "bmi_category", length = 50)
    private String bmiCategory;

    /**
     * 餐次类型
     */
    @Column(name = "meal_type", length = 20)
    private String mealType;

    /**
     * AI生成的完整JSON内容
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "JSON")
    private String contentJson;

    /**
     * 适用孕周
     */
    @Column(name = "pregnancy_week")
    private Integer pregnancyWeek;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

