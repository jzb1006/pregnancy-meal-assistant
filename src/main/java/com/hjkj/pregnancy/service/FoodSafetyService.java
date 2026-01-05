package com.hjkj.pregnancy.service;

import reactor.core.publisher.Flux;

/**
 * 食品安全检测服务
 *
 * @author Zhibin Jiang
 */
public interface FoodSafetyService {

    /**
     * 检查食物安全性 (流式)
     *
     * @param openId 用户OpenID
     * @param query  查询词 (如: 螃蟹)
     * @return 检查结果流 (JSON片段)
     */
    Flux<String> checkFoodSafety(String openId, String query);

    /**
     * 获取每日营养小贴士
     *
     * @param openId 用户OpenID
     * @return 贴士文案
     */
    String getNutritionTip(String openId);
}
