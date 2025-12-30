package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.AiRequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI请求日志Repository
 * 
 * @author Zhibin Jiang
 */
@Repository
public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {

    /**
     * 根据用户ID查询日志
     */
    List<AiRequestLog> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 根据用户ID分页查询日志
     */
    Page<AiRequestLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * 根据业务场景查询日志
     */
    List<AiRequestLog> findByScenarioOrderByCreatedAtDesc(String scenario);

    /**
     * 查询指定时间范围内的日志
     */
    List<AiRequestLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime startTime, 
        LocalDateTime endTime
    );

    /**
     * 查询失败的请求
     */
    List<AiRequestLog> findByIsSuccessFalseOrderByCreatedAtDesc();

    /**
     * 统计用户的AI请求次数
     */
    @Query("SELECT COUNT(a) FROM AiRequestLog a WHERE a.userId = :userId")
    Long countByUserId(String userId);

    /**
     * 统计指定时间范围内的请求次数
     */
    @Query("SELECT COUNT(a) FROM AiRequestLog a WHERE a.createdAt BETWEEN :startTime AND :endTime")
    Long countByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计平均响应时间
     */
    @Query("SELECT AVG(a.duration) FROM AiRequestLog a WHERE a.isSuccess = true")
    Double getAverageDuration();

    /**
     * 查询最近的N条日志
     */
    List<AiRequestLog> findTop10ByOrderByCreatedAtDesc();
}

