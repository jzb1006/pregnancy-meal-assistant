package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.model.dto.PrenatalCheckToggleRequest;
import com.hjkj.pregnancy.model.vo.NextPrenatalCheckVO;
import com.hjkj.pregnancy.model.vo.PrenatalCheckItemVO;
import com.hjkj.pregnancy.model.vo.PrenatalCheckTimelineVO;

/**
 * 产检时光轴服务接口
 * 
 * @author Zhibin Jiang
 */
public interface PrenatalCheckService {

    /**
     * 获取产检时光轴
     *
     * @param openId 用户唯一标识
     * @return 产检时光轴数据
     */
    PrenatalCheckTimelineVO getTimeline(String openId);

    /**
     * 切换产检完成状态
     *
     * @param openId 用户唯一标识
     * @param request 切换请求
     * @return 更新后的产检项目
     */
    PrenatalCheckItemVO toggleCheckStatus(String openId, PrenatalCheckToggleRequest request);

    /**
     * 获取下次产检信息
     *
     * @param openId 用户唯一标识
     * @return 下次产检信息
     */
    NextPrenatalCheckVO getNextCheck(String openId);
}

