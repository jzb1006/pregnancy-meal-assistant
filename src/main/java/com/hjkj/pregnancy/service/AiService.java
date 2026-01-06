package com.hjkj.pregnancy.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import com.hjkj.pregnancy.advisor.AiRequestAdvisor;
import com.hjkj.pregnancy.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI服务封装类
 * <p>
 * 封装AI调用的通用逻辑，包括普通调用和流式调用，统一处理拦截器和异常
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final DashScopeChatModel chatModel;
    private final AiRequestAdvisor aiRequestAdvisor;

    @Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
    private String aiModel;

    @Value("${spring.ai.dashscope.chat.options.temperature:0.7}")
    private Double aiTemperature;

    /**
     * 调用AI并转换为指定类型（带上下文）
     *
     * @param prompt    提示词
     * @param clazz     目标类型
     * @param context   Advisor上下文
     * @param <T>       目标类型
     * @return 转换后的对象
     * @throws AiServiceException AI服务异常
     */
    public <T> T callWithContext(String prompt, Class<T> clazz, AiAdvisorContext context) {
        try {
            log.debug("调用AI生成内容，Prompt长度: {}, 模型: {}, 目标类型: {}", 
                prompt.length(), aiModel, clazz.getSimpleName());

            BeanOutputConverter<T> converter = new BeanOutputConverter<>(clazz);
            String format = converter.getFormat();
            String fullPrompt = prompt + "\n\n" + format;

            Prompt aiPrompt = new Prompt(new UserMessage(fullPrompt));

            // 包装 Advisor 参数
            var advisorParams = AiRequestAdvisor.wrapContext(context);

            // 请求前拦截
            aiPrompt = aiRequestAdvisor.beforeRequest(aiPrompt, advisorParams);

            // 调用AI
            ChatResponse chatResponse = chatModel.call(aiPrompt);

            // 响应后拦截
            chatResponse = aiRequestAdvisor.afterResponse(chatResponse, advisorParams);

            String response = chatResponse.getResult().getOutput().getText();
            log.debug("AI返回结果长度: {}", response.length());

            return converter.convert(response);

        } catch (Exception e) {
            log.error("调用AI失败", e);
            // 错误拦截
            aiRequestAdvisor.onError(e, AiRequestAdvisor.wrapContext(context));
            throw new AiServiceException("AI服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * 流式调用AI（带上下文）
     *
     * @param prompt  提示词
     * @param context Advisor上下文
     * @return 流式响应
     * @throws AiServiceException AI服务异常
     */
    public Flux<ChatResponse> streamWithContext(String prompt, AiAdvisorContext context) {
        try {
            log.debug("开始流式调用AI，Prompt长度: {}, 模型: {}", prompt.length(), aiModel);

            // 构造 ChatOptions，显式指定模型
            DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                    .model(aiModel)
                    .temperature(aiTemperature)
                    .build();

            Prompt aiPrompt = new Prompt(new UserMessage(prompt), chatOptions);

            // 包装 Advisor 参数
            var advisorParams = AiRequestAdvisor.wrapContext(context);

            // 请求前拦截
            aiPrompt = aiRequestAdvisor.beforeRequest(aiPrompt, advisorParams);

            // 使用stream方法获取流式响应
            Flux<ChatResponse> responseFlux = chatModel.stream(aiPrompt);

            // 使用 Advisor 包装流式响应（自动处理拦截和日志）
            return aiRequestAdvisor.afterStreamResponse(responseFlux, advisorParams);

        } catch (Exception e) {
            log.error("流式调用AI失败", e);
            // 错误拦截
            aiRequestAdvisor.onError(e, AiRequestAdvisor.wrapContext(context));
            throw new AiServiceException("AI服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * 简单调用AI（不带上下文，用于测试或简单场景）
     *
     * @param prompt 提示词
     * @param clazz  目标类型
     * @param <T>    目标类型
     * @return 转换后的对象
     */
    public <T> T call(String prompt, Class<T> clazz) {
        AiAdvisorContext context = AiAdvisorContext.of("unknown", "simple_call", "NONE");
        return callWithContext(prompt, clazz, context);
    }

    /**
     * 简单流式调用（不带上下文）
     *
     * @param prompt 提示词
     * @return 流式响应
     */
    public Flux<ChatResponse> stream(String prompt) {
        AiAdvisorContext context = AiAdvisorContext.of("unknown", "simple_stream", "NONE");
        return streamWithContext(prompt, context);
    }
}





