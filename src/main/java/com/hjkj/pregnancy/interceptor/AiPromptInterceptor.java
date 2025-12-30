package com.hjkj.pregnancy.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI提示语拦截器
 * 拦截并记录用户发送给AI的提示语和AI的响应
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Component
public class AiPromptInterceptor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 拦截AI请求前
     * 
     * @param prompt 提示语对象
     * @param context 上下文信息（如用户ID、餐次类型等）
     * @return 可能被修改的prompt
     */
    public Prompt beforeRequest(Prompt prompt, InterceptorContext context) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(FORMATTER);
        
        // 提取所有消息内容
        List<Message> messages = prompt.getInstructions();
        String fullPrompt = messages.stream()
            .map(Message::getText)
            .collect(Collectors.joining("\n"));
        
        // 记录请求信息（DEBUG级别）
        if (log.isDebugEnabled()) {
            log.debug("=".repeat(80));
            log.debug("AI请求拦截 [{}]", timestamp);
            log.debug("=".repeat(80));
            log.debug("用户标识: {}", context.getUserId());
            log.debug("业务场景: {}", context.getScenario());
            log.debug("餐次类型: {}", context.getMealType());
            log.debug("Prompt长度: {} 字符", fullPrompt.length());
            log.debug("-".repeat(80));
            log.debug("完整Prompt:\n{}", fullPrompt);
            log.debug("=".repeat(80));
        }
        
        // 保存到上下文（供响应时使用）
        context.setRequestTime(now);
        context.setPromptContent(fullPrompt);
        
        // 可以在这里修改prompt（如添加敏感词过滤、内容审核等）
        // Prompt modifiedPrompt = modifyPromptIfNeeded(prompt);
        // return modifiedPrompt;
        
        return prompt;
    }

    /**
     * 拦截AI响应后
     * 
     * @param response AI的响应（可能为null，特别是在流式接口中）
     * @param context 上下文信息
     * @return 可能被修改的response
     */
    public ChatResponse afterResponse(ChatResponse response, InterceptorContext context) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(FORMATTER);
        
        // 计算耗时
        long duration = java.time.Duration.between(context.getRequestTime(), now).toMillis();
        
        // 提取响应内容（流式接口可能response为null，从context中获取）
        String responseContent = null;
        if (response != null && response.getResult() != null) {
            responseContent = response.getResult().getOutput().getText();
        } else if (context.getResponseContent() != null) {
            // 流式接口的情况，从context中获取
            responseContent = context.getResponseContent();
        } else {
            responseContent = "";
        }
        
        // 记录响应信息（DEBUG级别）
        if (log.isDebugEnabled()) {
            log.debug("=".repeat(80));
            log.debug("AI响应拦截 [{}]", timestamp);
            log.debug("=".repeat(80));
            log.debug("用户标识: {}", context.getUserId());
            log.debug("业务场景: {}", context.getScenario());
            log.debug("耗时: {} ms", duration);
            log.debug("响应长度: {} 字符", responseContent.length());
            log.debug("Token使用: {}", response != null ? response.getMetadata() : "N/A (流式接口)");
            log.debug("-".repeat(80));
            log.debug("AI响应内容:\n{}", responseContent);
            log.debug("=".repeat(80));
        }
        
        // 保存到上下文
        context.setResponseTime(now);
        context.setResponseContent(responseContent);
        context.setDuration(duration);
        
        // 可以在这里修改响应（如敏感信息过滤、内容格式化等）
        // ChatResponse modifiedResponse = modifyResponseIfNeeded(response);
        // return modifiedResponse;
        
        return response;
    }

    /**
     * 拦截异常
     * 
     * @param e 异常对象
     * @param context 上下文信息
     */
    public void onError(Exception e, InterceptorContext context) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(FORMATTER);
        
        // 计算耗时
        long duration = context.getRequestTime() != null 
            ? java.time.Duration.between(context.getRequestTime(), now).toMillis() 
            : 0;
        
        // 记录错误信息（简化）
        log.error("AI请求异常 - 用户: {}, 场景: {}, 耗时: {}ms, 异常: {}", 
            context.getUserId(), context.getScenario(), duration, e.getMessage());
        
        // 保存到上下文
        context.setErrorTime(now);
        context.setErrorMessage(e.getMessage());
    }

    /**
     * 拦截上下文
     * 用于在拦截器之间传递信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InterceptorContext {
        /**
         * 用户标识
         */
        private String userId;
        
        /**
         * 业务场景（如：meal_recommend）
         */
        private String scenario;
        
        /**
         * 餐次类型
         */
        private String mealType;
        
        /**
         * 请求时间
         */
        private LocalDateTime requestTime;
        
        /**
         * 响应时间
         */
        private LocalDateTime responseTime;
        
        /**
         * 错误时间
         */
        private LocalDateTime errorTime;
        
        /**
         * Prompt内容
         */
        private String promptContent;
        
        /**
         * 响应内容
         */
        private String responseContent;
        
        /**
         * 错误信息
         */
        private String errorMessage;
        
        /**
         * 耗时（毫秒）
         */
        private Long duration;
        
        /**
         * 扩展信息
         */
        private java.util.Map<String, Object> extras;
    }
}

