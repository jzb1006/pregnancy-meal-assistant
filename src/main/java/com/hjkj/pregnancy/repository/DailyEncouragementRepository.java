package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.DailyEncouragement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 每日鼓励语录数据访问层
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Repository
public interface DailyEncouragementRepository extends JpaRepository<DailyEncouragement, Long> {

    /**
     * 根据用户ID和日期查询记录
     *
     * @param openId     用户唯一标识
     * @param recordDate 记录日期
     * @return 鼓励语录记录
     */
    Optional<DailyEncouragement> findByOpenIdAndRecordDate(String openId, LocalDate recordDate);
}