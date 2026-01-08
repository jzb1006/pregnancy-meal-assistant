package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.model.dto.WeightRecordRequest;
import com.hjkj.pregnancy.model.vo.PrePregnancyWeightVO;
import com.hjkj.pregnancy.model.vo.WeightRecordVO;
import com.hjkj.pregnancy.model.vo.WeightStatsVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 体重管理服务接口
 * 
 * @author Zhibin Jiang
 */
public interface WeightService {

    /**
     * 保存体重记录
     * 同一天的记录会被更新而非新增
     *
     * @param openId 用户唯一标识
     * @param request 体重记录请求
     * @return 保存后的体重记录
     */
    WeightRecordVO saveWeightRecord(String openId, WeightRecordRequest request);

    /**
     * 获取体重历史记录
     *
     * @param openId 用户唯一标识
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param limit 返回条数限制（可选）
     * @return 体重记录列表
     */
    List<WeightRecordVO> getWeightHistory(String openId, LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 获取孕前体重
     *
     * @param openId 用户唯一标识
     * @return 孕前体重信息
     */
    PrePregnancyWeightVO getPrePregnancyWeight(String openId);

    /**
     * 获取体重统计信息
     *
     * @param openId 用户唯一标识
     * @return 体重统计信息
     */
    WeightStatsVO getWeightStats(String openId);

    /**
     * 删除体重记录
     *
     * @param openId 用户唯一标识
     * @param recordId 记录ID
     */
    void deleteWeightRecord(String openId, Long recordId);
}

