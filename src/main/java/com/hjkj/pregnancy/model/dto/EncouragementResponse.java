package com.hjkj.pregnancy.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 生成鼓励语录的返回格式 DTO
 * <p>
 * 用于 BeanOutputConverter 解析 AI 返回的 JSON 格式数据。
 * 对应 AI 提示词中定义的 Output Format。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncouragementResponse {

    /**
     * 鼓励语录（50字以内，包含 emoji）
     */
    @JsonProperty("encouragement")
    private String encouragement;

    /**
     * 宝宝状态描述（如"像个柠檬"）
     */
    @JsonProperty("babySize")
    private String babySize;
}