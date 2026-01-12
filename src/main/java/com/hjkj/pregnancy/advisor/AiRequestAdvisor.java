package com.hjkj.pregnancy.advisor;

import com.hjkj.pregnancy.service.AiLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI 请求响应拦截器（Advisor）
 * <p>实现对所有 AI 请求的自动拦截，记录请求和响应信息。
 * 该 Advisor 会自动应用到所有使用 ChatModel 的调用中。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>请求前拦截：记录 Prompt 内容和业务上下文</li>
 *   <li>响应后拦截：记录响应内容、耗时和 Token 使用情况</li>
 *   <li>异常拦截：记录错误信息</li>
 *   <li>异步日志保存：不影响主流程性能</li>
 * </ul>
 *
 * <p>使用方式：</p>
 * <pre>{@code
 * // 创建上下文
 * AiAdvisorContext context = AiAdvisorContext.of(openId, "meal_recommend", "BREAKFAST");
 * 
 * // 构造 ChatOptions（将上下文传递给 Advisor）
 * Map<String, Object> advisorParams = Map.of("context", context);
 * DashScopeChatOptions options = DashScopeChatOptions.builder()
 *     .model("qwen3-max")
 *     .build();
 * 
 * // 调用 AI（Advisor 自动拦截）
 * ChatResponse response = chatModel.call(prompt, options, advisorParams);
 * }</pre>
 *
 * @author Zhibin Jiang
 * @since 1.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiRequestAdvisor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CONTEXT_KEY = "context";

    private final AiLogService aiLogService;

    /**
     * 请求前拦截
     * <p>记录 Prompt 内容和业务上下文信息</p>
     *
     * @param prompt 原始 Prompt
     * @param params Advisor 参数（包含业务上下文）
     * @return 可能被修改的 Prompt
     */
    public Prompt beforeRequest(Prompt prompt, Map<String, Object> params) {
        AiAdvisorContext context = extractContext(params);
        if (context == null) {
            log.warn("AiAdvisorContext 为空，无法记录请求信息");
            return prompt;
        }

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(FORMATTER);

        // 提取所有消息内容
        String fullPrompt = prompt.getInstructions().stream()
                .map(msg -> msg.getText())
                .collect(Collectors.joining("\n"));

        // 从 Prompt 的 Options 中提取模型名称
        String modelName = extractModelNameFromPrompt(prompt);
        context.setModelName(modelName);

        // 记录请求信息（DEBUG 级别）
        if (log.isDebugEnabled()) {
            log.debug("=".repeat(80));
            log.debug("AI 请求拦截 [{}]", timestamp);
            log.debug("=".repeat(80));
            log.debug("用户标识: {}", context.getUserId());
            log.debug("业务场景: {}", context.getScenario());
            log.debug("餐次类型: {}", context.getMealType());
            log.debug("模型名称: {}", modelName);
            log.debug("Prompt 长度: {} 字符", fullPrompt.length());
            log.debug("-".repeat(80));
            log.debug("完整 Prompt:\n{}", fullPrompt);
            log.debug("=".repeat(80));
        }

        // 保存到上下文（供响应时使用）
        context.setRequestTime(now);
        context.setPromptContent(fullPrompt);

        // 可以在这里修改 prompt（如添加敏感词过滤、内容审核等）
        return prompt;
    }

    /**
     * 响应后拦截（普通调用）
     * <p>记录响应内容、耗时和 Token 使用情况</p>
     *
     * @param response AI 响应
     * @param params   Advisor 参数（包含业务上下文）
     * @return 可能被修改的响应
     */
    public ChatResponse afterResponse(ChatResponse response, Map<String, Object> params) {
        AiAdvisorContext context = extractContext(params);
        if (context == null) {
            log.warn("AiAdvisorContext 为空，无法记录响应信息");
            return response;
        }

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(FORMATTER);

        // 计算耗时
        long duration = context.getRequestTime() != null
                ? Duration.between(context.getRequestTime(), now).toMillis()
                : 0;

        // 提取响应内容
        String responseContent = "";
        if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
            responseContent = response.getResult().getOutput().getText();
        }

        // 提取 Token 使用量
        extractTokenUsage(response, context);

        // 记录响应信息（DEBUG 级别）
        if (log.isDebugEnabled()) {
            log.debug("=".repeat(80));
            log.debug("AI 响应拦截 [{}]", timestamp);
            log.debug("=".repeat(80));
            log.debug("用户标识: {}", context.getUserId());
            log.debug("业务场景: {}", context.getScenario());
            log.debug("模型名称: {}", context.getModelName());
            log.debug("耗时: {} ms", duration);
            log.debug("响应长度: {} 字符", responseContent.length());
            log.debug("Token 使用: 输入={}, 输出={}, 总计={}", 
                    context.getInputTokens(), context.getOutputTokens(), context.getTotalTokens());
            log.debug("-".repeat(80));
            log.debug("AI 响应内容:\n{}", responseContent);
            log.debug("=".repeat(80));
        }

        // 保存到上下文
        context.setResponseTime(now);
        context.setResponseContent(responseContent);
        context.setDuration(duration);

        // 异步保存日志
        CompletableFuture.runAsync(() -> aiLogService.saveLog(context));

        // 可以在这里修改响应（如敏感信息过滤、内容格式化等）
        return response;
    }

    /**
     * 响应后拦截（流式调用）
     * <p>处理流式响应，记录完整的响应内容</p>
     *
     * @param responseFlux 流式响应
     * @param params       Advisor 参数（包含业务上下文）
     * @return 可能被修改的流式响应
     */
    public Flux<ChatResponse> afterStreamResponse(Flux<ChatResponse> responseFlux, Map<String, Object> params) {
        AiAdvisorContext context = extractContext(params);
        if (context == null) {
            log.warn("AiAdvisorContext 为空，无法记录流式响应信息");
            return responseFlux;
        }

        StringBuilder fullResponse = new StringBuilder();
        // 用于保存最后一个 ChatResponse（包含完整的 Token 信息）
        final ChatResponse[] lastResponse = new ChatResponse[1];

        return responseFlux
                .doOnNext(chatResponse -> {
                    // 累积响应内容
                    if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                        String content = chatResponse.getResult().getOutput().getText();
                        fullResponse.append(content);
                        
                        if (log.isDebugEnabled()) {
                            log.debug("收到流式 chunk: {}", content);
                        }
                    }
                    // 保存最后一个响应（包含完整的 Token 信息）
                    lastResponse[0] = chatResponse;
                })
                .doOnComplete(() -> {
                    // 流式响应完成
                    LocalDateTime now = LocalDateTime.now();
                    String timestamp = now.format(FORMATTER);

                    // 计算耗时
                    long duration = context.getRequestTime() != null
                            ? Duration.between(context.getRequestTime(), now).toMillis()
                            : 0;

                    // 从最后一个响应中提取 Token 使用量
                    extractTokenUsage(lastResponse[0], context);

                    if (log.isDebugEnabled()) {
                        log.debug("=".repeat(80));
                        log.debug("AI 流式响应完成 [{}]", timestamp);
                        log.debug("=".repeat(80));
                        log.debug("用户标识: {}", context.getUserId());
                        log.debug("业务场景: {}", context.getScenario());
                        log.debug("模型名称: {}", context.getModelName());
                        log.debug("耗时: {} ms", duration);
                        log.debug("完整响应长度: {} 字符", fullResponse.length());
                        log.debug("Token 使用: 输入={}, 输出={}, 总计={}", 
                                context.getInputTokens(), context.getOutputTokens(), context.getTotalTokens());
                        log.debug("-".repeat(80));
                        log.debug("完整响应内容:\n{}", fullResponse);
                        log.debug("=".repeat(80));
                    }

                    // 保存到上下文
                    context.setResponseTime(now);
                    context.setResponseContent(fullResponse.toString());
                    context.setDuration(duration);

                    // 异步保存日志
                    CompletableFuture.runAsync(() -> aiLogService.saveLog(context));
                })
                .doOnError(error -> {
                    // 流式响应错误
                    onError(error, params);
                });
    }

    /**
     * 异常拦截
     * <p>记录错误信息并异步保存日志</p>
     *
     * @param error  异常对象
     * @param params Advisor 参数（包含业务上下文）
     */
    public void onError(Throwable error, Map<String, Object> params) {
        AiAdvisorContext context = extractContext(params);
        if (context == null) {
            log.error("AI 请求异常（无上下文）: {}", error.getMessage(), error);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 计算耗时
        long duration = context.getRequestTime() != null
                ? Duration.between(context.getRequestTime(), now).toMillis()
                : 0;

        // 记录错误信息
        log.error("AI 请求异常 - 用户: {}, 场景: {}, 耗时: {}ms, 异常: {}",
                context.getUserId(), context.getScenario(), duration, error.getMessage(), error);

        // 保存到上下文
        context.setErrorTime(now);
        context.setErrorMessage(error.getMessage());
        context.setDuration(duration);

        // 异步保存错误日志
        CompletableFuture.runAsync(() -> aiLogService.saveLog(context));
    }

    /**
     * 从 Advisor 参数中提取业务上下文
     *
     * @param params Advisor 参数
     * @return AiAdvisorContext 或 null
     */
    private AiAdvisorContext extractContext(Map<String, Object> params) {
        if (params == null || !params.containsKey(CONTEXT_KEY)) {
            return null;
        }

        Object contextObj = params.get(CONTEXT_KEY);
        if (contextObj instanceof AiAdvisorContext) {
            return (AiAdvisorContext) contextObj;
        }

        log.warn("Advisor 参数中的 context 类型不正确: {}", 
                contextObj != null ? contextObj.getClass().getName() : "null");
        return null;
    }

    /**
     * 从 Prompt 的 ChatOptions 中提取模型名称
     *
     * @param prompt 请求的 Prompt
     * @return 模型名称，如果无法提取则返回 "unknown"
     */
    private String extractModelNameFromPrompt(Prompt prompt) {
        if (prompt == null) {
            return "unknown";
        }

        try {
            // 从 Prompt 的 Options 中获取模型名称
            ChatOptions options = prompt.getOptions();
            if (options != null && options.getModel() != null && !options.getModel().isEmpty()) {
                return options.getModel();
            }
            
            // 如果没有模型信息，返回 unknown
            return "unknown";
        } catch (Exception e) {
            log.warn("从 Prompt 提取模型名称失败: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * 将业务上下文包装为 Advisor 参数
     * <p>用于业务代码调用时传递上下文</p>
     *
     * @param context 业务上下文
     * @return Advisor 参数 Map
     */
    public static Map<String, Object> wrapContext(AiAdvisorContext context) {
        return Map.of(CONTEXT_KEY, context);
    }

    /**
     * 从 ChatResponse 提取 Token 使用量
     * <p>提取输入、输出和总 Token 数量，并保存到上下文中</p>
     *
     * @param response AI 响应对象
     * @param context  业务上下文
     */
    private void extractTokenUsage(ChatResponse response, AiAdvisorContext context) {
        if (response == null || response.getMetadata() == null) {
            log.debug("ChatResponse 或 Metadata 为空，无法提取 Token 使用量");
            return;
        }

        try {
            var usage = response.getMetadata().getUsage();
            if (usage != null) {
                // 提取 Token 数据（Spring AI 1.1.0 返回 Integer）
                Integer promptTokens = usage.getPromptTokens();
                Integer completionTokens = usage.getCompletionTokens();
                Integer totalTokens = usage.getTotalTokens();

                // 保存到上下文
                if (promptTokens != null) {
                    context.setInputTokens(promptTokens);
                }
                if (completionTokens != null) {
                    context.setOutputTokens(completionTokens);
                }
                if (totalTokens != null) {
                    context.setTotalTokens(totalTokens);
                } else if (promptTokens != null && completionTokens != null) {
                    // 如果没有提供 totalTokens，自行计算
                    context.setTotalTokens(promptTokens + completionTokens);
                }

                log.debug("成功提取 Token 使用量: 输入={}, 输出={}, 总计={}", 
                        context.getInputTokens(), context.getOutputTokens(), context.getTotalTokens());
            } else {
                log.debug("Usage 信息为空，无法提取 Token 使用量");
            }
        } catch (Exception e) {
            log.warn("提取 Token 使用量失败: {}", e.getMessage());
        }
    }
}

