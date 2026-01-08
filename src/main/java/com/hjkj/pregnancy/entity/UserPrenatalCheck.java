package com.hjkj.pregnancy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户产检完成状态实体类
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_prenatal_check",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_openid_code",
           columnNames = {"open_id", "template_code"}
       ))
public class UserPrenatalCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户唯一标识（微信OpenID）
     */
    @Column(name = "open_id", nullable = false, length = 64)
    private String openId;

    /**
     * 产检项目编码
     */
    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    /**
     * 是否完成
     */
    @Column(name = "is_done", nullable = false)
    private Boolean isDone;

    /**
     * 实际检查日期
     */
    @Column(name = "check_date")
    private LocalDate checkDate;

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
        if (isDone == null) {
            isDone = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

