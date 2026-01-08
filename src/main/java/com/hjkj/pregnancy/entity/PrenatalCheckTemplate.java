package com.hjkj.pregnancy.entity;

import com.hjkj.pregnancy.enums.PregnancyStage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产检标准模板实体类
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prenatal_check_template")
public class PrenatalCheckTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目编码（唯一标识）
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 孕周范围-开始
     */
    @Column(name = "week_range_start", nullable = false)
    private Integer weekRangeStart;

    /**
     * 孕周范围-结束
     */
    @Column(name = "week_range_end", nullable = false)
    private Integer weekRangeEnd;

    /**
     * 产检名称
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 简短描述
     */
    @Column(name = "short_desc", length = 200)
    private String shortDesc;

    /**
     * 详细说明
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * 注意事项
     */
    @Column(name = "tips", columnDefinition = "TEXT")
    private String tips;

    /**
     * 孕期阶段
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 20)
    private PregnancyStage stage;

    /**
     * 阶段标题
     */
    @Column(name = "stage_title", nullable = false, length = 50)
    private String stageTitle;

    /**
     * 阶段图标
     */
    @Column(name = "stage_icon", length = 10)
    private String stageIcon;

    /**
     * 排序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /**
     * 是否启用
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

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
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

