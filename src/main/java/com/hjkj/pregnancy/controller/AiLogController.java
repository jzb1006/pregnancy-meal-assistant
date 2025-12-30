package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.entity.AiRequestLog;
import com.hjkj.pregnancy.service.AiLogService;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI日志查询控制器
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@RestController
@RequestMapping("/v1/ai-log")
@RequiredArgsConstructor
@Tag(name = "AI日志管理", description = "AI请求日志查询相关接口")
public class AiLogController {

    private final AiLogService aiLogService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户的AI请求日志", description = "根据用户ID查询所有AI请求记录")
    public Result<List<AiRequestLog>> getUserLogs(
            @Parameter(description = "用户唯一标识", required = true)
            @PathVariable String userId) {
        try {
            log.info("查询用户AI日志: userId={}", userId);
            List<AiRequestLog> logs = aiLogService.getUserLogs(userId);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("查询用户AI日志失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/recent")
    @Operation(summary = "查询最近的AI请求日志", description = "查询最近10条AI请求记录")
    public Result<List<AiRequestLog>> getRecentLogs() {
        try {
            log.info("查询最近AI日志");
            List<AiRequestLog> logs = aiLogService.getRecentLogs(10);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("查询最近AI日志失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/failed")
    @Operation(summary = "查询失败的AI请求", description = "查询所有失败的AI请求记录")
    public Result<List<AiRequestLog>> getFailedLogs() {
        try {
            log.info("查询失败的AI日志");
            List<AiRequestLog> logs = aiLogService.getFailedLogs();
            return Result.success(logs);
        } catch (Exception e) {
            log.error("查询失败的AI日志失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/range")
    @Operation(summary = "按时间范围查询AI请求日志", description = "查询指定时间范围内的AI请求记录")
    public Result<List<AiRequestLog>> getLogsByTimeRange(
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            log.info("按时间范围查询AI日志: {} ~ {}", startTime, endTime);
            List<AiRequestLog> logs = aiLogService.getLogsByTimeRange(startTime, endTime);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("按时间范围查询AI日志失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stats/user/{userId}")
    @Operation(summary = "统计用户的AI请求", description = "统计用户的AI请求次数")
    public Result<Map<String, Object>> getUserStats(
            @Parameter(description = "用户唯一标识", required = true)
            @PathVariable String userId) {
        try {
            log.info("统计用户AI请求: userId={}", userId);
            Long count = aiLogService.countUserRequests(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("userId", userId);
            stats.put("totalRequests", count);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("统计用户AI请求失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stats/performance")
    @Operation(summary = "查询AI性能统计", description = "查询AI的平均响应时间等性能指标")
    public Result<Map<String, Object>> getPerformanceStats() {
        try {
            log.info("查询AI性能统计");
            Double avgDuration = aiLogService.getAverageDuration();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("averageDuration", avgDuration != null ? avgDuration : 0);
            stats.put("unit", "毫秒");
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("查询AI性能统计失败", e);
            return Result.error(e.getMessage());
        }
    }
}

