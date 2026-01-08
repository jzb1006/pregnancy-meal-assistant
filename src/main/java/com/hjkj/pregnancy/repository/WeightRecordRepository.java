package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 体重记录数据访问接口
 * 
 * @author Zhibin Jiang
 */
@Repository
public interface WeightRecordRepository extends JpaRepository<WeightRecord, Long> {

    /**
     * 查询用户所有体重记录，按日期倒序（最新的在前）
     *
     * @param openId 用户唯一标识
     * @return 体重记录列表
     */
    List<WeightRecord> findByOpenIdOrderByRecordDateDesc(String openId);

    /**
     * 查询用户指定日期范围的体重记录，按日期倒序（最新的在前）
     *
     * @param openId 用户唯一标识
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 体重记录列表
     */
    @Query("SELECT w FROM WeightRecord w WHERE w.openId = :openId " +
           "AND w.recordDate >= :startDate AND w.recordDate <= :endDate " +
           "ORDER BY w.recordDate DESC")
    List<WeightRecord> findByOpenIdAndDateRange(
        @Param("openId") String openId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 查询用户指定日期的记录
     *
     * @param openId 用户唯一标识
     * @param recordDate 记录日期
     * @return 体重记录（可选）
     */
    Optional<WeightRecord> findByOpenIdAndRecordDate(String openId, LocalDate recordDate);

    /**
     * 查询用户最新的体重记录
     *
     * @param openId 用户唯一标识
     * @return 最新的体重记录（可选）
     */
    Optional<WeightRecord> findFirstByOpenIdOrderByRecordDateDesc(String openId);

    /**
     * 统计用户体重记录总数
     *
     * @param openId 用户唯一标识
     * @return 记录总数
     */
    long countByOpenId(String openId);

    /**
     * 删除用户指定记录
     *
     * @param id 记录ID
     * @param openId 用户唯一标识
     */
    void deleteByIdAndOpenId(Long id, String openId);
}

