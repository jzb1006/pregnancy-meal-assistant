package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import com.hjkj.pregnancy.entity.AiRequestLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI日志服务接口
 * 
 * @author Zhibin Jiang
 */
public interface AiLogService {

    /**
     * 保存AI请求日志
     * 
     * @param context Advisor 上下文
     * @return 保存的日志
     */
    AiRequestLog saveLog(AiAdvisorContext context);

    /**
     * 根据用户ID查询日志
     * 
     * @param userId 用户ID
     * @return 日志列表
     */
    List<AiRequestLog> getUserLogs(String userId);

    /**
     * 查询最近的日志
     * 
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AiRequestLog> getRecentLogs(int limit);

    /**
     * 查询失败的请求
     * 
     * @return 日志列表
     */
    List<AiRequestLog> getFailedLogs();

    /**
     * 统计用户的请求次数
     * 
     * @param userId 用户ID
     * @return 请求次数
     */
    Long countUserRequests(String userId);

    /**
     * 查询指定时间范围内的日志
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<AiRequestLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取平均响应时间
     * 
     * @return 平均响应时间（毫秒）
     */
    Double getAverageDuration();
}

