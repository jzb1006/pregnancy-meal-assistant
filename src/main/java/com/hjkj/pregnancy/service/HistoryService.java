package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.model.vo.MealVO;

import java.util.List;

/**
 * 浏览历史服务接口
 * 
 * @author Zhibin Jiang
 */
public interface HistoryService {

    /**
     * 记录浏览历史
     * 
     * @param userId 用户ID
     * @param recipeId 食谱ID
     */
    void recordHistory(Long userId, Long recipeId);

    /**
     * 获取用户最近浏览的食谱ID列表
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 食谱ID列表
     */
    List<Long> getRecentRecipeIds(Long userId, int limit);

    /**
     * 获取用户浏览历史
     * 
     * @param openId 用户唯一标识
     * @return 浏览历史列表
     */
    List<MealVO> getUserHistory(String openId);

    /**
     * 获取用户最近浏览的菜品名称列表
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 菜品名称列表
     */
    List<String> getRecentDishNames(Long userId, int limit);
}

