package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.model.vo.MealVO;
import reactor.core.publisher.Flux;

public interface DailyRecommendationService {

    /**
     * 获取每日推荐 (流式)
     *
     * @param openId 用户OpenID
     * @return 推荐结果流
     */
    Flux<String> getDailyRecommendation(String openId);

    /**
     * 换一换 (流式)
     *
     * @param openId 用户OpenID
     * @return 新的推荐结果流
     */
    Flux<String> swapRecommendation(String openId);
}
