package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import com.hjkj.pregnancy.entity.AiRequestLog;
import com.hjkj.pregnancy.repository.AiRequestLogRepository;
import com.hjkj.pregnancy.service.AiLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI日志服务实现类
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiLogServiceImpl implements AiLogService {

    private final AiRequestLogRepository aiRequestLogRepository;

    @Override
    @Transactional
    public AiRequestLog saveLog(AiAdvisorContext context) {
        try {
            AiRequestLog logEntity = AiRequestLog.builder()
                .userId(context.getUserId())
                .scenario(context.getScenario())
                .mealType(context.getMealType())
                .promptContent(context.getPromptContent())
                .promptLength(context.getPromptContent() != null ? context.getPromptContent().length() : 0)
                .responseContent(context.getResponseContent())
                .responseLength(context.getResponseContent() != null ? context.getResponseContent().length() : 0)
                .duration(context.getDuration())
                .isSuccess(context.getErrorMessage() == null)
                .errorMessage(context.getErrorMessage())
                .modelName(context.getModelName() != null ? context.getModelName() : "unknown")
                .tokenUsage(context.getTotalTokens()) // 保存 Token 使用量
                .createdAt(context.getRequestTime())
                .build();

            AiRequestLog saved = aiRequestLogRepository.save(logEntity);
            log.info("AI请求日志已保存: logId={}, userId={}, scenario={}, tokens={}", 
                saved.getId(), saved.getUserId(), saved.getScenario(), saved.getTokenUsage());
            
            return saved;
            
        } catch (Exception e) {
            log.error("保存AI请求日志失败", e);
            // 不抛出异常，避免影响主业务流程
            return null;
        }
    }

    @Override
    public List<AiRequestLog> getUserLogs(String userId) {
        return aiRequestLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<AiRequestLog> getRecentLogs(int limit) {
        return aiRequestLogRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public List<AiRequestLog> getFailedLogs() {
        return aiRequestLogRepository.findByIsSuccessFalseOrderByCreatedAtDesc();
    }

    @Override
    public Long countUserRequests(String userId) {
        return aiRequestLogRepository.countByUserId(userId);
    }

    @Override
    public List<AiRequestLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return aiRequestLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startTime, endTime);
    }

    @Override
    public Double getAverageDuration() {
        return aiRequestLogRepository.getAverageDuration();
    }
}

