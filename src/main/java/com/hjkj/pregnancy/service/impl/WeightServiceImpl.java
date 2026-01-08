package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.entity.WeightRecord;
import com.hjkj.pregnancy.enums.BmiCategory;
import com.hjkj.pregnancy.exception.BusinessException;
import com.hjkj.pregnancy.exception.ErrorCode;
import com.hjkj.pregnancy.model.dto.WeightRecordRequest;
import com.hjkj.pregnancy.model.vo.PrePregnancyWeightVO;
import com.hjkj.pregnancy.model.vo.WeightRecordVO;
import com.hjkj.pregnancy.model.vo.WeightStatsVO;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.repository.WeightRecordRepository;
import com.hjkj.pregnancy.service.WeightService;
import com.hjkj.pregnancy.utils.BmiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 体重管理服务实现类
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeightServiceImpl implements WeightService {

    private final WeightRecordRepository weightRecordRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public WeightRecordVO saveWeightRecord(String openId, WeightRecordRequest request) {
        log.info("保存体重记录: openId={}, date={}, weight={}", openId, request.getDate(), request.getWeight());

        // 获取用户档案以计算孕周
        UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 计算孕周
        Integer pregnancyWeek = calculatePregnancyWeek(userProfile.getLastMenstrualPeriod(), request.getDate());

        // 查找是否已有同日期记录
        WeightRecord record = weightRecordRepository
                .findByOpenIdAndRecordDate(openId, request.getDate())
                .orElse(WeightRecord.builder()
                        .openId(openId)
                        .recordDate(request.getDate())
                        .build());

        // 更新字段
        record.setWeight(request.getWeight());
        record.setPregnancyWeek(pregnancyWeek);
        record.setNote(request.getNote());

        WeightRecord saved = weightRecordRepository.save(record);
        log.info("体重记录保存成功: id={}", saved.getId());

        return convertToVO(saved);
    }

    @Override
    public List<WeightRecordVO> getWeightHistory(String openId, LocalDate startDate, LocalDate endDate, Integer limit) {
        log.info("查询体重历史: openId={}, startDate={}, endDate={}, limit={}", openId, startDate, endDate, limit);

        List<WeightRecord> records;
        
        if (startDate != null && endDate != null) {
            records = weightRecordRepository.findByOpenIdAndDateRange(openId, startDate, endDate);
        } else {
            records = weightRecordRepository.findByOpenIdOrderByRecordDateDesc(openId);
        }

        // 应用limit（已经是倒序，直接取前N条）
        if (limit != null && limit > 0 && records.size() > limit) {
            records = records.subList(0, limit);
        }

        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public PrePregnancyWeightVO getPrePregnancyWeight(String openId) {
        log.info("获取孕前体重: openId={}", openId);

        UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Double currentWeight = userProfile.getCurrentWeight();
        if (currentWeight == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "用户未设置孕前体重");
        }

        return PrePregnancyWeightVO.builder()
                .weight(BigDecimal.valueOf(currentWeight))
                .source("user_profile")
                .build();
    }

    @Override
    public WeightStatsVO getWeightStats(String openId) {
        log.info("获取体重统计: openId={}", openId);

        UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 获取孕前体重
        Double prePregnancyWeight = userProfile.getCurrentWeight();
        if (prePregnancyWeight == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "用户未设置孕前体重");
        }

        // 获取最新体重记录
        WeightRecord latestRecord = weightRecordRepository
                .findFirstByOpenIdOrderByRecordDateDesc(openId)
                .orElse(null);

        BigDecimal currentWeight = latestRecord != null 
                ? latestRecord.getWeight() 
                : BigDecimal.valueOf(prePregnancyWeight);

        BigDecimal weightGain = currentWeight.subtract(BigDecimal.valueOf(prePregnancyWeight));

        // 计算当前孕周
        Integer currentWeek = calculatePregnancyWeek(userProfile.getLastMenstrualPeriod(), LocalDate.now());

        // 计算BMI
        Integer height = userProfile.getHeight();
        BigDecimal bmi = null;
        BmiCategory bmiCategory = null;
        
        if (height != null && height > 0) {
            double bmiValue = BmiUtil.calculateBmi(height, currentWeight.doubleValue());
            bmi = BigDecimal.valueOf(bmiValue);
            bmiCategory = BmiUtil.getBmiCategoryEnum(bmiValue);
        }

        return WeightStatsVO.builder()
                .currentWeight(currentWeight)
                .prePregnancyWeight(BigDecimal.valueOf(prePregnancyWeight))
                .weightGain(weightGain)
                .currentWeek(currentWeek)
                .bmi(bmi)
                .bmiCategory(bmiCategory)
                .build();
    }

    @Override
    @Transactional
    public void deleteWeightRecord(String openId, Long recordId) {
        log.info("删除体重记录: openId={}, recordId={}", openId, recordId);
        weightRecordRepository.deleteByIdAndOpenId(recordId, openId);
    }

    /**
     * 计算孕周
     *
     * @param lmp 末次月经日期
     * @param targetDate 目标日期
     * @return 孕周
     */
    private Integer calculatePregnancyWeek(LocalDate lmp, LocalDate targetDate) {
        if (lmp == null || targetDate == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(lmp, targetDate);
        return (int) (days / 7);
    }

    /**
     * 转换为VO
     *
     * @param record 体重记录实体
     * @return 体重记录VO
     */
    private WeightRecordVO convertToVO(WeightRecord record) {
        return WeightRecordVO.builder()
                .id(record.getId())
                .date(record.getRecordDate())
                .weight(record.getWeight())
                .week(record.getPregnancyWeek())
                .note(record.getNote())
                .build();
    }
}

