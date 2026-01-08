package com.hjkj.pregnancy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体重记录实体类
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "weight_record", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_openid_date", 
           columnNames = {"open_id", "record_date"}
       ))
public class WeightRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户唯一标识（微信OpenID）
     */
    @Column(name = "open_id", nullable = false, length = 64)
    private String openId;

    /**
     * 记录日期
     */
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    /**
     * 体重(kg)
     */
    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    /**
     * 孕周
     */
    @Column(name = "pregnancy_week")
    private Integer pregnancyWeek;

    /**
     * 备注
     */
    @Column(name = "note", length = 500)
    private String note;

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

