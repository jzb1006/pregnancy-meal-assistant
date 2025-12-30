package com.hjkj.pregnancy.model.dto;

import com.hjkj.pregnancy.enums.FeedbackAction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户反馈请求参数
 *
 * @author Zhibin Jiang
 */
@Data
public class FeedbackRequest {
    @NotNull(message = "OpenID不能为空")
    private String openId;

    @NotNull(message = "食谱ID不能为空")
    private Long recipeId;

    @NotNull(message = "动作不能为空")
    private FeedbackAction action; // LIKE, DISLIKE, BORED

    private String reason;
}
