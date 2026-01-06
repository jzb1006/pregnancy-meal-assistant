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
     * 流式推荐食谱（使用SSE）
     * 
     * @param openId   用户唯一标识
     * @param mealType 餐次类型：BREAKFAST/LUNCH/DINNER
     * @return SSE发射器
     */
    SseEmitter recommendMealStream(String openId, String mealType);

    /**
     * 推荐食谱(Flux版本,用于聚合为完整JSON)
     * 
     * @param openId   用户唯一标识
     * @param mealType 餐次类型:BREAKFAST/LUNCH/DINNER
     * @return Flux流,最终会聚合为完整JSON字符串
     */
    reactor.core.publisher.Flux<String> recommendMealFlux(String openId, String mealType);
}
