package com.hjkj.pregnancy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 宫缩记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contraction_record")
public class ContractionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String openId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 持续时长（秒）: endTime - startTime
     */
    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    /**
     * 间隔时长（秒）: 本次 startTime - 上次 startTime
     */
    @Column(name = "interval_seconds")
    private Integer intervalSeconds;

    /**
     * 疼痛等级 (1-10) - Optional
     */
    @Column(name = "pain_level")
    private Integer painLevel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
