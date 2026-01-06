package com.hjkj.pregnancy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 浏览历史搜索请求DTO
 * <p>
 * 封装历史记录搜索的所有查询参数，支持多条件组合搜索。
 * 所有搜索条件均为可选，未指定的条件将被忽略。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-01-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "浏览历史搜索请求参数")
public class HistorySearchRequest {

    /**
     * 菜单名称（支持模糊搜索）
     */
    @Schema(description = "菜单名称（模糊搜索）", example = "牛肉")
    private String dishName;

    /**
     * 用户反馈动作（LIKE/DISLIKE/BORED）
     */
    @Schema(description = "反馈动作：LIKE-喜欢, DISLIKE-不喜欢, BORED-吃腻了", 
            example = "LIKE",
            allowableValues = {"LIKE", "DISLIKE", "BORED"})
    @Pattern(regexp = "^(LIKE|DISLIKE|BORED)$", 
             message = "反馈动作只能是 LIKE、DISLIKE 或 BORED")
    private String feedbackAction;

    /**
     * 餐次类型（BREAKFAST/LUNCH/DINNER）
     */
    @Schema(description = "餐次类型：BREAKFAST-早餐, LUNCH-午餐, DINNER-晚餐", 
            example = "LUNCH",
            allowableValues = {"BREAKFAST", "LUNCH", "DINNER"})
    @Pattern(regexp = "^(BREAKFAST|LUNCH|DINNER)$", 
             message = "餐次类型只能是 BREAKFAST、LUNCH 或 DINNER")
    private String mealType;

    /**
     * 页码（从1开始）
     */
    @Schema(description = "页码（从1开始）", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码必须大于0")
    @Builder.Default
    private int page = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    @Builder.Default
    private int size = 10;

    /**
     * 检查是否有任何搜索条件
     *
     * @return 如果设置了任何搜索条件返回 true
     */
    public boolean hasSearchConditions() {
        return (dishName != null && !dishName.isBlank()) 
                || (feedbackAction != null && !feedbackAction.isBlank())
                || (mealType != null && !mealType.isBlank());
    }
}

