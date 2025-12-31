package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.UserHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户浏览历史数据访问层
 * 
 * @author Zhibin Jiang
 */
@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    /**
     * 查询用户最近浏览的食谱ID列表
     * 
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 食谱ID列表
     */
    @Query("""
            SELECT h.recipeId FROM UserHistory h
            WHERE h.userId = :userId
            ORDER BY h.viewedAt DESC
            LIMIT :limit
            """)
    List<Long> findRecentRecipeIds(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询用户所有浏览历史
     * 
     * @param userId 用户ID
     * @return 浏览历史列表
     */
    List<UserHistory> findByUserIdOrderByViewedAtDesc(Long userId);

    /**
     * 分页查询用户浏览历史
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 分页浏览历史
     */
    Page<UserHistory> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);

    /**
     * 删除用户的某条浏览记录
     * 
     * @param userId   用户ID
     * @param recipeId 食谱ID
     */
    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);
}
