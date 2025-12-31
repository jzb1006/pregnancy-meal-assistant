package com.hjkj.pregnancy.entity;

import com.hjkj.pregnancy.enums.MoodType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日鼓励语录实体类
 * <p>
 * 独立存储每日的心情和鼓励语录数据，支持历史记录追踪。
 * 通过 openId + recordDate 唯一索引保证每天只有一条记录。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_encouragement",
        uniqueConstraints = @UniqueConstraint(name = "uk_open_id_date", columnNames = {"open_id", "record_date"}),
        indexes = {
                @Index(name = "idx_open_id", columnList = "open_id"),
                @Index(name = "idx_record_date", columnList = "record_date")
        })
public class DailyEncouragement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户唯一标识（微信OpenID）
     */
    @Column(name = "open_id", nullable = false, length = 64)
    private String openId;

    /**
     * 当日心情
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mood", nullable = false, length = 16)
    private MoodType mood;

    /**
     * 鼓励语录文本
     */
    @Column(name = "encouragement_text", nullable = false, length = 200)
    private String encouragementText;

    /**
     * 宝宝状态描述（如"像个柠檬"）
     */
    @Column(name = "baby_size", nullable = false, length = 32)
    private String babySize;

    /**
     * 孕周
     */
    @Column(name = "week", nullable = false)
    private Integer week;

    /**
     * 生成时间
     */
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    /**
     * 记录日期（用于唯一索引和查询）
     */
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    /**
     * 是否为降级文案
     */
    @Column(name = "is_fallback", nullable = false)
    private Boolean isFallback;

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