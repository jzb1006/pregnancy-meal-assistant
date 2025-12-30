package com.hjkj.pregnancy.model.dto;

import com.hjkj.pregnancy.enums.FeedbackAction;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 不喜欢的菜品信息 DTO
 * <p>
 * 用于查询用户不喜欢的菜品名称和原因，反馈给 AI 进行规避
 * </p>
 *
 * @param dishName 菜品名称
 * @param reason   不喜欢的原因
 * @param action   反馈动作 (DISLIKE/BORED)
 */
@Schema(description = "不喜欢的菜品信息")
public record DislikedDishDTO(
        @Schema(description = "菜品名称") String dishName,

        @Schema(description = "不喜欢的原因") String reason,

        @Schema(description = "反馈动作") FeedbackAction action) {
}
