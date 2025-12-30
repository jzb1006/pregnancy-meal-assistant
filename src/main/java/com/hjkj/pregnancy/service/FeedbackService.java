package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.enums.FeedbackAction;

import java.util.List;

/**
 * 用户反馈服务接口
 *
 * @author Zhibin Jiang
 */
public interface FeedbackService {

    /**
     * 提交反馈
     *
     * @param openId   用户OpenID
     * @param recipeId 食谱ID
     * @param action   动作 (LIKE/DISLIKE/BORED)
     * @param reason   原因
     */
    void submitFeedback(String openId, Long recipeId, FeedbackAction action, String reason);

    /**
     * 获取用户不感兴趣的食谱ID
     *
     * @param userId 用户ID
     * @return ID列表
     */
    List<Long> getDislikedRecipeIds(Long userId);

    /**
     * 获取用户最近不喜欢的菜品详情（用于AI Prompt）
     *
     * @param userId 用户ID
     * @return DTO列表
     */
    List<com.hjkj.pregnancy.model.dto.DislikedDishDTO> getRecentDislikedDishes(Long userId);
}
