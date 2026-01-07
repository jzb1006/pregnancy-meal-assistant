package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.DailyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyRecommendationRepository extends JpaRepository<DailyRecommendation, Long> {

    /**
     * 根据用户ID和日期查询今日推荐（返回最新的一条）
     * 使用 First 关键字确保即使有重复数据也只返回第一条
     */
    Optional<DailyRecommendation> findFirstByUserIdAndRecDateOrderByCreatedAtDesc(Long userId, LocalDate recDate);
}
