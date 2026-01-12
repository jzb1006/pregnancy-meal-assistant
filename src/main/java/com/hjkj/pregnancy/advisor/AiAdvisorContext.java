package com.hjkj.pregnancy.advisor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 请求拦截器上下文
 * <p>用于在 Advisor 中传递业务信息，包括用户标识、业务场景、餐次类型等。
 * 该上下文会通过 ChatOptions 传递给 RequestResponseAdvisor。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * AiAdvisorContext context = AiAdvisorContext.builder()
 *     .userId(openId)
 *     .scenario("meal_recommend")
 *     .mealType("BREAKFAST")
 *     .build();
 * }</pre>
 *
 * @author Zhibin Jiang
 * @since 1.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAdvisorContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户标识（如 openId）
     */
    private String userId;

    /**
     * 业务场景
     * <p>如：meal_recommend（普通推荐）、meal_recommend_stream（流式推荐）</p>
     */
    private String scenario;

    /**
     * 餐次类型
     * <p>如：BREAKFAST、LUNCH、DINNER</p>
     */
    private String mealType;

    /**
     * AI 模型名称
     * <p>如：qwen3-max、qwen-plus，由 Advisor 自动提取</p>
     */
    private String modelName;

    /**
     * 请求时间
     * <p>由 Advisor 自动设置</p>
     */
    private LocalDateTime requestTime;

    /**
     * 响应时间
     * <p>由 Advisor 自动设置</p>
     */
    private LocalDateTime responseTime;

    /**
     * 错误时间
     * <p>发生异常时由 Advisor 自动设置</p>
     */
    private LocalDateTime errorTime;

    /**
     * Prompt 内容
     * <p>由 Advisor 自动提取并设置</p>
     */
    private String promptContent;

    /**
     * 响应内容
     * <p>由 Advisor 自动提取并设置</p>
     */
    private String responseContent;

    /**
     * 错误信息
     * <p>发生异常时由 Advisor 自动设置</p>
     */
    private String errorMessage;

    /**
     * 请求耗时（毫秒）
     * <p>由 Advisor 自动计算并设置</p>
     */
    private Long duration;

    /**
     * 输入 Token 数量
     * <p>由 Advisor 从 ChatResponse 自动提取</p>
     */
    private Integer inputTokens;

    /**
     * 输出 Token 数量
     * <p>由 Advisor 从 ChatResponse 自动提取</p>
     */
    private Integer outputTokens;

    /**
     * 总 Token 数量
     * <p>由 Advisor 从 ChatResponse 自动提取，等于 inputTokens + outputTokens</p>
     */
    private Integer totalTokens;

    /**
     * 扩展信息
     * <p>用于传递额外的业务信息</p>
     */
    @Builder.Default
    private Map<String, Object> extras = new java.util.HashMap<>();

    /**
     * 创建一个简单的上下文（仅包含必要的业务信息）
     *
     * @param userId   用户标识
     * @param scenario 业务场景
     * @param mealType 餐次类型
     * @return AiAdvisorContext 实例
     */
    public static AiAdvisorContext of(String userId, String scenario, String mealType) {
        return AiAdvisorContext.builder()
                .userId(userId)
                .scenario(scenario)
                .mealType(mealType)
                .build();
    }

    /**
     * 添加扩展信息
     *
     * @param key   键
     * @param value 值
     * @return 当前实例（支持链式调用）
     */
    public AiAdvisorContext putExtra(String key, Object value) {
        if (this.extras == null) {
            this.extras = new java.util.HashMap<>();
        }
        this.extras.put(key, value);
        return this;
    }

    /**
     * 获取扩展信息
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtra(String key) {
        return extras != null ? (T) extras.get(key) : null;
    }
}

