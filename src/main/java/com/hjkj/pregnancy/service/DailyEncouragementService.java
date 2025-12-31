package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.enums.MoodType;
import com.hjkj.pregnancy.model.vo.DailyEncouragementVO;

/**
 * 每日鼓励语录服务接口
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
public interface DailyEncouragementService {

    /**
     * 获取每日鼓励语录
     * <p>
     * 幂等操作：同一天内多次调用返回相同内容。
     * 如果今日未生成，则调用 AI 生成并存储。
     * </p>
     *
     * @param openId 用户唯一标识
     * @param mood   当前心情（可选，默认 HAPPY）
     * @return 每日鼓励语录
     */
    DailyEncouragementVO getDailyEncouragement(String openId, MoodType mood);
}