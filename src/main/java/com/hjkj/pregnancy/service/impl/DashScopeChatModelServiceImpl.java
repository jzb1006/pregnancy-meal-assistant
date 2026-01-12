package com.hjkj.pregnancy.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import com.hjkj.pregnancy.advisor.AiRequestAdvisor;
import com.hjkj.pregnancy.exception.AiServiceException;
import com.hjkj.pregnancy.service.ChatModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * <b>DashScopeChatModelServiceImpl.java</b>
 *
 * <pre>
 * 阿里云通义千问 AI 服务实现类，封装阿里云 DashScope 的 AI 调用逻辑
 * </pre>
 * <p>
 * 该实现类提供统一的接口供业务层使用，支持普通调用和流式调用，
 * 并自动集成请求/响应拦截器进行日志记录和 Token 统计。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>封装 DashScopeChatModel 的调用细节</li>
 *   <li>自动转换 JSON 响应为 Java 对象（使用 BeanOutputConverter）</li>
 *   <li>集成 AiRequestAdvisor 实现请求/响应拦截</li>
 *   <li>自动提取和记录 Token 使用量</li>
 *   <li>统一的异常处理和日志记录</li>
 *   <li>支持通过 ChatOptions 自定义 AI 调用参数</li>
 * </ul>
 *
 * <p>配置说明：</p>
 * <ul>
 *   <li>模型名称：通过 {@code spring.ai.dashscope.chat.options.model} 配置，默认 qwen3-max</li>
 *   <li>温度参数：通过 {@code spring.ai.dashscope.chat.options.temperature} 配置，默认 0.7</li>
 * </ul>
 *
 * @author ZhibinJiang ZhibinJiang@chatlabs.cn
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashScopeChatModelServiceImpl implements ChatModelService {

    private final DashScopeChatModel chatModel;
    private final AiRequestAdvisor aiRequestAdvisor;

    @Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
    private String aiModel;

    @Value("${spring.ai.dashscope.chat.options.temperature:0.7}")
    private Double aiTemperature;

    /**
     * <b>buildChatOptions</b> <br/>
     * 构建 ChatOptions 配置对象
     * <p>
     * 根据 customOptions 参数决定使用自定义配置还是默认配置：
     * </p>
     * <ul>
     *   <li>如果 customOptions 不为 null，直接使用传入的自定义配置</li>
     *   <li>如果 customOptions 为 null，使用配置文件的默认值（aiModel 和 aiTemperature）</li>
     * </ul>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null）
     * @return {@link ChatOptions} ChatOptions 实例
     *
     */
    private ChatOptions buildChatOptions(ChatOptions customOptions) {
        if (customOptions != null) {
            log.debug("使用自定义 ChatOptions: {}", customOptions);
            return customOptions;
        }

        ChatOptions defaultOptions = DashScopeChatOptions.builder()
                .model(aiModel)
                .temperature(aiTemperature)
                .build();
        log.debug("使用默认 ChatOptions: model={}, temperature={}", aiModel, aiTemperature);
        return defaultOptions;
    }

    /**
     * <b>call</b> <br/>
     * 调用 AI 并转换为指定类型（带上下文）
     * <p>
     * 该方法为接口 {@link ChatModelService#call(String, Class, AiAdvisorContext)} 的实现，
     * 内部调用支持自定义 ChatOptions 的重载方法，传入 null 表示使用默认配置。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param clazz {@link Class} 目标类型的 Class 对象
     * @param context {@link AiAdvisorContext} AI 请求上下文
     * @param <T> 目标类型
     * @return {@link T} 转换后的对象
     * @throws AiServiceException AI 服务异常
     *
     */
    @Override
    public <T> T call(String prompt, Class<T> clazz, AiAdvisorContext context) {
        return call(prompt, clazz, context, null);
    }

    /**
     * <b>call</b> <br/>
     * 调用 AI 并转换为指定类型（带上下文，支持自定义 ChatOptions）
     * <p>
     * 该方法为核心实现方法，完成以下操作：
     * </p>
     * <ul>
     *   <li>构建 ChatOptions（使用默认或自定义配置）</li>
     *   <li>使用 BeanOutputConverter 自动生成 JSON Schema 并追加到 Prompt</li>
     *   <li>通过 AiRequestAdvisor 进行请求前拦截</li>
     *   <li>调用底层 DashScopeChatModel 执行 AI 生成</li>
     *   <li>通过 AiRequestAdvisor 进行响应后拦截（自动提取 Token）</li>
     *   <li>将 JSON 响应转换为目标 Java 对象</li>
     *   <li>异常时通过 AiRequestAdvisor 进行错误拦截</li>
     * </ul>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param clazz {@link Class} 目标类型的 Class 对象
     * @param context {@link AiAdvisorContext} AI 请求上下文
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null）
     * @param <T> 目标类型
     * @return {@link T} 转换后的对象
     * @throws AiServiceException AI 服务异常
     *
     */
    @Override
    public <T> T call(String prompt, Class<T> clazz, AiAdvisorContext context, ChatOptions customOptions) {
        try {
            // 构建 ChatOptions（使用默认或自定义）
            ChatOptions chatOptions = buildChatOptions(customOptions);

            log.debug("调用 AI 生成内容，Prompt 长度: {}, 目标类型: {}",
                    prompt.length(), clazz.getSimpleName());

            // 使用 BeanOutputConverter 自动生成 JSON Schema
            BeanOutputConverter<T> converter = new BeanOutputConverter<>(clazz);
            String format = converter.getFormat();
            String fullPrompt = prompt + "\n\n" + format;

            // 构建 Prompt
            Prompt aiPrompt = new Prompt(new UserMessage(fullPrompt), chatOptions);

            // 包装 Advisor 参数
            var advisorParams = AiRequestAdvisor.wrapContext(context);

            // 请求前拦截
            aiPrompt = aiRequestAdvisor.beforeRequest(aiPrompt, advisorParams);

            // 调用 AI
            ChatResponse chatResponse = chatModel.call(aiPrompt);

            // 响应后拦截（自动提取 Token）
            chatResponse = aiRequestAdvisor.afterResponse(chatResponse, advisorParams);

            // 提取响应内容
            String response = chatResponse.getResult().getOutput().getText();
            log.debug("AI 返回结果长度: {}", response.length());

            // 转换为目标类型
            return converter.convert(response);

        } catch (Exception e) {
            log.error("调用 AI 失败", e);
            // 错误拦截
            aiRequestAdvisor.onError(e, AiRequestAdvisor.wrapContext(context));
            throw new AiServiceException("AI 服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * <b>stream</b> <br/>
     * 流式调用 AI（带上下文）
     * <p>
     * 该方法为接口 {@link ChatModelService#stream(String, AiAdvisorContext)} 的实现，
     * 内部调用支持自定义 ChatOptions 的重载方法，传入 null 表示使用默认配置。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param context {@link AiAdvisorContext} AI 请求上下文
     * @return {@link Flux}&lt;{@link ChatResponse}&gt; 流式响应
     * @throws AiServiceException AI 服务异常
     *
     */
    @Override
    public Flux<ChatResponse> stream(String prompt, AiAdvisorContext context) {
        return stream(prompt, context, null);
    }

    /**
     * <b>stream</b> <br/>
     * 流式调用 AI（带上下文，支持自定义 ChatOptions）
     * <p>
     * 该方法为核心流式调用实现，完成以下操作：
     * </p>
     * <ul>
     *   <li>构建 ChatOptions（使用默认或自定义配置）</li>
     *   <li>通过 AiRequestAdvisor 进行请求前拦截</li>
     *   <li>调用底层 DashScopeChatModel 的 stream 方法获取流式响应</li>
     *   <li>通过 AiRequestAdvisor 包装流式响应（自动处理拦截和日志，包括 Token 提取）</li>
     *   <li>异常时通过 AiRequestAdvisor 进行错误拦截</li>
     * </ul>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param context {@link AiAdvisorContext} AI 请求上下文
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null）
     * @return {@link Flux}&lt;{@link ChatResponse}&gt; 流式响应
     * @throws AiServiceException AI 服务异常
     *
     */
    @Override
    public Flux<ChatResponse> stream(String prompt, AiAdvisorContext context, ChatOptions customOptions) {
        try {
            // 构建 ChatOptions（使用默认或自定义）
            ChatOptions chatOptions = buildChatOptions(customOptions);

            log.debug("开始流式调用 AI，Prompt 长度: {}", prompt.length());

            Prompt aiPrompt = new Prompt(new UserMessage(prompt), chatOptions);

            // 包装 Advisor 参数
            var advisorParams = AiRequestAdvisor.wrapContext(context);

            // 请求前拦截
            aiPrompt = aiRequestAdvisor.beforeRequest(aiPrompt, advisorParams);

            // 使用 stream 方法获取流式响应
            Flux<ChatResponse> responseFlux = chatModel.stream(aiPrompt);

            // 使用 Advisor 包装流式响应（自动处理拦截和日志，包括 Token 提取）
            return aiRequestAdvisor.afterStreamResponse(responseFlux, advisorParams);

        } catch (Exception e) {
            log.error("流式调用 AI 失败", e);
            // 错误拦截
            aiRequestAdvisor.onError(e, AiRequestAdvisor.wrapContext(context));
            throw new AiServiceException("AI 服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * <b>call</b> <br/>
     * 简单调用 AI（不带上下文，用于测试或简单场景）
     * <p>
     * 该方法为接口 {@link ChatModelService#call(String, Class)} 的实现，
     * 内部调用支持自定义 ChatOptions 的重载方法，传入 null 表示使用默认配置。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param clazz {@link Class} 目标类型的 Class 对象
     * @param <T> 目标类型
     * @return {@link T} 转换后的对象
     *
     */
    @Override
    public <T> T call(String prompt, Class<T> clazz) {
        return call(prompt, clazz, (AiAdvisorContext) null);
    }

    /**
     * <b>call</b> <br/>
     * 简单调用 AI（不带上下文，支持自定义 ChatOptions）
     * <p>
     * 该方法为接口 {@link ChatModelService#call(String, Class, ChatOptions)} 的实现，
     * 内部创建一个默认的上下文对象（用户 ID 为 "unknown"，场景为 "simple_call"），
     * 然后调用完整的 call 方法。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param clazz {@link Class} 目标类型的 Class 对象
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null）
     * @param <T> 目标类型
     * @return {@link T} 转换后的对象
     *
     */
    @Override
    public <T> T call(String prompt, Class<T> clazz, ChatOptions customOptions) {
        AiAdvisorContext context = AiAdvisorContext.of("unknown", "simple_call", "NONE");
        return call(prompt, clazz, context, customOptions);
    }

    /**
     * <b>stream</b> <br/>
     * 简单流式调用（不带上下文）
     * <p>
     * 该方法为接口 {@link ChatModelService#stream(String)} 的实现，
     * 内部调用支持自定义 ChatOptions 的重载方法，传入 null 表示使用默认配置。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @return {@link Flux}&lt;{@link ChatResponse}&gt; 流式响应
     *
     */
    @Override
    public Flux<ChatResponse> stream(String prompt) {
        return stream(prompt, (AiAdvisorContext) null);
    }

    /**
     * <b>stream</b> <br/>
     * 简单流式调用（不带上下文，支持自定义 ChatOptions）
     * <p>
     * 该方法为接口 {@link ChatModelService#stream(String, ChatOptions)} 的实现，
     * 内部创建一个默认的上下文对象（用户 ID 为 "unknown"，场景为 "simple_stream"），
     * 然后调用完整的 stream 方法。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null）
     * @return {@link Flux}&lt;{@link ChatResponse}&gt; 流式响应
     *
     */
    @Override
    public Flux<ChatResponse> stream(String prompt, ChatOptions customOptions) {
        AiAdvisorContext context = AiAdvisorContext.of("unknown", "simple_stream", "NONE");
        return stream(prompt, context, customOptions);
    }
}
