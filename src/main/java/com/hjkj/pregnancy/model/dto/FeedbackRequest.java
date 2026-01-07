package com.hjkj.pregnancy.model.dto;

import com.hjkj.pregnancy.enums.FeedbackAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户反馈请求参数
 * <p>
 * openId不需要传递，会从登录token中自动获取
 * </p>
 *
 * @author Zhibin Jiang
 */
@Data
@Schema(description = "用户反馈请求")
public class FeedbackRequest {

    @Schema(description = "食谱ID", required = true, example = "123")
    @NotNull(message = "食谱ID不能为空")
    private Long recipeId;

    @Schema(description = "反馈动作：LIKE-喜欢/DISLIKE-不喜欢/BORED-吃腻了", required = true, example = "LIKE")
    @NotNull(message = "动作不能为空")
    private FeedbackAction action;

    @Schema(description = "反馈原因（可选）", example = "太辣了")
    private String reason;
}
