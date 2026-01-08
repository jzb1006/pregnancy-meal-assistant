package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.entity.FetalMovementRecord;
import com.hjkj.pregnancy.model.dto.FetalMovementRequest;

import java.util.List;

public interface FetalMovementService {

    /**
     * 保存胎动记录
     */
    FetalMovementRecord saveRecord(String openId, FetalMovementRequest request);

    /**
     * 获取最近的胎动记录
     */
    List<FetalMovementRecord> getHistory(String openId);
}
