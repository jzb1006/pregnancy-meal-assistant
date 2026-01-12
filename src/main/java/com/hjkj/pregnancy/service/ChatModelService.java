package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import reactor.core.publisher.Flux;

/**
 * <b>ChatModelService.java</b>
 *
 * <pre>
 * AI 聊天模型服务接口，提供统一的 AI 模型调用抽象
 * </pre>
 * <p>
 * 该接口支持多种 AI 服务提供商（如阿里云通义千问、OpenAI 等），
 * 封装了 AI 调用的底层细节，提供简洁的 API 供业务层使用。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>普通调用：同步调用 AI 并将响应转换为指定类型</li>
 *   <li>流式调用：支持 Server-Sent Events (SSE) 流式响应</li>
 *   <li>自动拦截：集成请求/响应拦截器，自动记录日志和 Token 使用量</li>
 *   <li>异常处理：统一的异常处理和降级策略</li>
 *   <li>自定义配置：支持通过 {@link ChatOptions} 自定义 AI 调用参数</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 普通调用（带上下文）
 * AiAdvisorContext context = AiAdvisorContext.of(openId, "meal_recommend", "BREAKFAST");
 * MealVO result = chatModelService.call(prompt, MealVO.class, context);
 *
 * // 流式调用（带上下文）
 * Flux<ChatResponse> stream = chatModelService.stream(prompt, context);
 *
 * // 简化调用（不带上下文）
 * MealVO result = chatModelService.call(prompt, MealVO.class);
 *
 * // 自定义配置调用
 * DashScopeChatOptions options = DashScopeChatOptions.builder()
 *     .model("qwen-plus")
 *     .temperature(0.5)
 *     .build();
 * MealVO result = chatModelService.call(prompt, MealVO.class, context, options);
 * }</pre>
 *
 * @author ZhibinJiang ZhibinJiang@chatlabs.cn
 * @since 1.0.0
 */
public interface ChatModelService {

    /**
     * <b>call</b> <br/>
     * 调用 AI 模型并转换为指定类型（带上下文）
     * <p>
     * 该方法会自动完成以下操作：
     * </p>
     * <ul>
     *   <li>构建 AI 请求的 Prompt</li>
     *   <li>调用底层 AI 模型</li>
     *   <li>将 JSON 响应转换为指定的 Java 对象</li>
     *   <li>记录请求/响应日志和 Token 使用量</li>
     * </ul>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param clazz {@link Class} 目标类型的 Class 对象
     * @param context {@link AiAdvisorContext} AI 请求上下文（包含用户 ID、业务场景等信息）
     * @param <T> 目标类型
     * @return {@link T} 转换后的 Java 对象
     * @throws com.hjkj.pregnancy.exception.AiServiceException 当 AI 调用失败时抛出
     *
     */
    <T> T call(String prompt, Class<T> clazz, AiAdvisorContext context);

    /**
     * <b>stream</b> <br/>
     * 流式调用 AI 模型（带上下文）
     * <p>
     * 该方法返回 Reactive Flux 流，适用于需要实时展示生成内容的场景（如打字机效果）。
     * 每个 ChatResponse 包含一个文本片段（chunk），客户端可以逐步接收并展示。
     * </p>
     *
     * <p>流式调用的优势：</p>
     * <ul>
     *   <li>降低首字延迟：用户无需等待完整响应，可以立即看到开始生成的内容</li>
     *   <li>更好的用户体验：类似打字机效果，更加流畅自然</li>
     *   <li>实时反馈：可以在生成过程中进行取消或调整</li>
     * </ul>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param context {@link AiAdvisorContext} AI 请求上下文（包含用户 ID、业务场景等信息）
     * @return {@link Flux}&lt;{@link ChatResponse}&gt; Reactive Flux 流，每个元素是一个 ChatResponse
     * @throws com.hjkj.pregnancy.exception.AiServiceException 当 AI 调用失败时抛出
     *
     */
    Flux<ChatResponse> stream(String prompt, AiAdvisorContext context);

    /**
     * <b>call</b> <br/>
     * 简化调用 AI 模型（不带上下文）
     * <p>
     * 适用于测试或简单场景，不需要记录详细的业务上下文。
     * 内部会创建一个默认的上下文对象。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param clazz {@link Class} 目标类型的 Class 对象
     * @param <T> 目标类型
     * @return {@link T} 转换后的 Java 对象
     * @throws com.hjkj.pregnancy.exception.AiServiceException 当 AI 调用失败时抛出
     *
     */
    <T> T call(String prompt, Class<T> clazz);

    /**
     * <b>stream</b> <br/>
     * 简化流式调用（不带上下文）
     * <p>
     * 适用于测试或简单场景，不需要记录详细的业务上下文。
     * 内部会创建一个默认的上下文对象。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @return {@link Flux}&lt;{@link ChatResponse}&gt; Reactive Flux 流，每个元素是一个 ChatResponse
     * @throws com.hjkj.pregnancy.exception.AiServiceException 当 AI 调用失败时抛出
     *
     */
    Flux<ChatResponse> stream(String prompt);

    /**
     * <b>call</b> <br/>
     * 调用 AI 模型并转换为指定类型（带上下文，支持自定义 ChatOptions）
     * <p>
     * 该方法允许自定义 AI 调用的参数（如模型名称、温度、Top-P 等）。
     * 如果 customOptions 为 null，则使用配置文件的默认值。
     * </p>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * // 自定义模型和温度
     * DashScopeChatOptions options = DashScopeChatOptions.builder()
     *     .model("qwen-plus")
     *     .temperature(0.5)
     *     .build();
     * MealVO result = chatModelService.call(prompt, MealVO.class, context, options);
     *
     * // 使用默认配置（customOptions 为 null）
     * MealVO result = chatModelService.call(prompt, MealVO.class, context, null);
     * }</pre>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param clazz {@link Class} 目标类型的 Class 对象
     * @param context {@link AiAdvisorContext} AI 请求上下文（包含用户 ID、业务场景等信息）
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null，null 时使用默认配置）
     * @param <T> 目标类型
     * @return {@link T} 转换后的 Java 对象
     * @throws com.hjkj.pregnancy.exception.AiServiceException 当 AI 调用失败时抛出
     *
     */
    <T> T call(String prompt, Class<T> clazz, AiAdvisorContext context, ChatOptions customOptions);

    /**
     * <b>stream</b> <br/>
     * 流式调用 AI 模型（带上下文，支持自定义 ChatOptions）
     * <p>
     * 该方法允许自定义 AI 调用的参数（如模型名称、温度、Top-P 等）。
     * 如果 customOptions 为 null，则使用配置文件的默认值。
     * </p>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * // 自定义模型
     * DashScopeChatOptions options = DashScopeChatOptions.builder()
     *     .model("qwen-turbo")
     *     .build();
     * Flux<ChatResponse> stream = chatModelService.stream(prompt, context, options);
     * }</pre>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param context {@link AiAdvisorContext} AI 请求上下文（包含用户 ID、业务场景等信息）
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null，null 时使用默认配置）
     * @return {@link Flux}&lt;{@link ChatResponse}&gt; Reactive Flux 流，每个元素是一个 ChatResponse
     * @throws com.hjkj.pregnancy.exception.AiServiceException 当 AI 调用失败时抛出
     *
     */
    Flux<ChatResponse> stream(String prompt, AiAdvisorContext context, ChatOptions customOptions);

    /**
     * <b>call</b> <br/>
     * 简化调用 AI 模型（不带上下文，支持自定义 ChatOptions）
     * <p>
     * 适用于测试或简单场景，不需要记录详细的业务上下文。
     * 内部会创建一个默认的上下文对象。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param clazz {@link Class} 目标类型的 Class 对象
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null，null 时使用默认配置）
     * @param <T> 目标类型
     * @return {@link T} 转换后的 Java 对象
     * @throws com.hjkj.pregnancy.exception.AiServiceException 当 AI 调用失败时抛出
     *
     */
    <T> T call(String prompt, Class<T> clazz, ChatOptions customOptions);

    /**
     * <b>stream</b> <br/>
     * 简化流式调用（不带上下文，支持自定义 ChatOptions）
     * <p>
     * 适用于测试或简单场景，不需要记录详细的业务上下文。
     * 内部会创建一个默认的上下文对象。
     * </p>
     *
     * @author ZhibinJiang ZhibinJiang@chatlabs.cn
     * @param prompt {@link String} AI 提示词
     * @param customOptions {@link ChatOptions} 自定义的 ChatOptions（可为 null，null 时使用默认配置）
     * @return {@link Flux}&lt;{@link ChatResponse}&gt; Reactive Flux 流，每个元素是一个 ChatResponse
     * @throws com.hjkj.pregnancy.exception.AiServiceException 当 AI 调用失败时抛出
     *
     */
    Flux<ChatResponse> stream(String prompt, ChatOptions customOptions);
}
