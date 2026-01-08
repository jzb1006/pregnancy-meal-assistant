package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.PrenatalCheckTemplate;
import com.hjkj.pregnancy.enums.PregnancyStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 产检模板数据访问接口
 * 
 * @author Zhibin Jiang
 */
@Repository
public interface PrenatalCheckTemplateRepository extends JpaRepository<PrenatalCheckTemplate, Long> {

    /**
     * 查询所有启用的产检模板，按孕周时间线排序（开始孕周升序，相同则按排序号升序）
     *
     * @return 产检模板列表（按时间线顺序）
     */
    List<PrenatalCheckTemplate> findByIsActiveTrueOrderByWeekRangeStartAscSortOrderAsc();

    /**
     * 根据编码查询产检模板
     *
     * @param code 项目编码
     * @return 产检模板（可选）
     */
    Optional<PrenatalCheckTemplate> findByCode(String code);

    /**
     * 查询指定阶段的产检模板
     *
     * @param stage 孕期阶段
     * @return 产检模板列表
     */
    List<PrenatalCheckTemplate> findByStageAndIsActiveTrueOrderBySortOrderAsc(PregnancyStage stage);
}

