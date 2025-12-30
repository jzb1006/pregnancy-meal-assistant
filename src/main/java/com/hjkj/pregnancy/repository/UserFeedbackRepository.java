package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.UserFeedback;
import com.hjkj.pregnancy.enums.FeedbackAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户反馈数据访问层
 * 
 * @author Zhibin Jiang
 */
@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {

    /**
     * 获取用户不喜欢的食谱ID列表
     * 
     * @param userId  用户ID
     * @param actions 反馈动作列表
     * @return 不喜欢的食谱ID列表
     */
    @Query("SELECT f.recipeId FROM UserFeedback f WHERE f.userId = :userId AND f.action IN :actions")
    List<Long> findRecipeIdsByUserIdAndActionIn(@Param("userId") Long userId,
            @Param("actions") List<FeedbackAction> actions);

    /**
     * 获取用户最近的不喜欢反馈详情
     *
     * @param userId   用户ID
     * @param actions  反馈动作列表
     * @param pageable 分页参数(用于限制数量)
     * @return DTO列表
     */
    @Query("SELECT new com.hjkj.pregnancy.model.dto.DislikedDishDTO(r.dishName, f.reason, f.action) " +
            "FROM UserFeedback f JOIN Recipe r ON f.recipeId = r.id " +
            "WHERE f.userId = :userId AND f.action IN :actions " +
            "ORDER BY f.createdAt DESC")
    List<com.hjkj.pregnancy.model.dto.DislikedDishDTO> findRecentDislikedDishes(
            @Param("userId") Long userId,
            @Param("actions") List<FeedbackAction> actions,
            org.springframework.data.domain.Pageable pageable);
}
