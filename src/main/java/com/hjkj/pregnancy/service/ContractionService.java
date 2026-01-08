package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.entity.ContractionRecord;
import com.hjkj.pregnancy.model.dto.ContractionRequest;

import java.util.List;

public interface ContractionService {

    /**
     * 保存宫缩记录
     */
    ContractionRecord saveRecord(String openId, ContractionRequest request);

    /**
     * 获取最近的宫缩记录
     */
    List<ContractionRecord> getHistory(String openId);
}
