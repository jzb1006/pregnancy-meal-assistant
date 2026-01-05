package com.hjkj.pregnancy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日食谱推荐实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_recommendation", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "rec_date" })
})
public class DailyRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rec_date", nullable = false)
    private LocalDate recDate;

    @Column(name = "week_num", nullable = false)
    private Integer weekNum;

    @Column(name = "dish_name", nullable = false, length = 100)
    private String dishName;

    /**
     * AI 生成的完整菜谱 JSON
     * 存储为 String，Service 层需自行序列化/反序列化
     */
    @Column(name = "content_json", nullable = false, columnDefinition = "JSON")
    private String contentJson;

    /**
     * 今日已拒绝列表(逗号分隔)
     */
    @Column(name = "rejected_history", columnDefinition = "TEXT")
    private String rejectedHistory;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
