package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.model.vo.MealVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 智能推荐服务接口
 * 
 * @author Zhibin Jiang
 */
public interface RecommendationService {

    /**
     * 智能推荐食谱（核心接口）
     * 
     * @param openId 用户唯一标识
     * @param mealType 餐次类型：BREAKFAST/LUNCH/DINNER
     * @return 推荐的食谱
     */
    MealVO recommendMeal(String openId, String mealType);

    /**
     * 流式推荐食谱（使用SSE）
     * 
     * @param openId 用户唯一标识
     * @param mealType 餐次类型：BREAKFAST/LUNCH/DINNER
     * @return SSE发射器
     */
    SseEmitter recommendMealStream(String openId, String mealType);
}

