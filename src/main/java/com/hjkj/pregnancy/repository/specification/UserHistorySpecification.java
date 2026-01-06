package com.hjkj.pregnancy.repository.specification;

import com.hjkj.pregnancy.entity.Recipe;
import com.hjkj.pregnancy.entity.UserFeedback;
import com.hjkj.pregnancy.entity.UserHistory;
import com.hjkj.pregnancy.enums.FeedbackAction;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/**
 * 用户浏览历史查询规格构建器
 * <p>
 * 提供基于 JPA Specification 的动态查询条件构建方法，支持多表关联查询和条件组合。
 * 使用 Criteria API 实现类型安全的查询构建。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-01-06
 */
public class UserHistorySpecification {

    /**
     * 私有构造函数，防止实例化
     */
    private UserHistorySpecification() {
    }

    /**
     * 根据用户ID过滤
     *
     * @param userId 用户ID
     * @return 查询规格
     */
    public static Specification<UserHistory> byUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("userId"), userId);
        };
    }

    /**
     * 根据菜单名称模糊搜索（关联 Recipe 表）
     *
     * @param dishName 菜单名称（支持模糊匹配）
     * @return 查询规格
     */
    public static Specification<UserHistory> byDishName(String dishName) {
        return (root, query, criteriaBuilder) -> {
            if (dishName == null || dishName.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            // 手动 JOIN Recipe 表（因为 UserHistory 中没有 @ManyToOne 关联）
            // 使用子查询方式实现
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Recipe> recipeRoot = subquery.from(Recipe.class);
            
            subquery.select(recipeRoot.get("id"))
                    .where(criteriaBuilder.like(
                        recipeRoot.get("dishName"),
                        "%" + dishName.trim() + "%"
                    ));

            return criteriaBuilder.in(root.get("recipeId")).value(subquery);
        };
    }

    /**
     * 根据用户反馈动作过滤（关联 UserFeedback 表）
     *
     * @param feedbackAction 反馈动作（LIKE/DISLIKE/BORED）
     * @return 查询规格
     */
    public static Specification<UserHistory> byFeedbackAction(FeedbackAction feedbackAction) {
        return (root, query, criteriaBuilder) -> {
            if (feedbackAction == null) {
                return criteriaBuilder.conjunction();
            }

            // Subquery 方式：查询存在指定反馈的历史记录
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<UserFeedback> feedbackRoot = subquery.from(UserFeedback.class);
            
            subquery.select(feedbackRoot.get("recipeId"))
                    .where(
                        criteriaBuilder.and(
                            criteriaBuilder.equal(feedbackRoot.get("userId"), root.get("userId")),
                            criteriaBuilder.equal(feedbackRoot.get("action"), feedbackAction)
                        )
                    );

            return criteriaBuilder.in(root.get("recipeId")).value(subquery);
        };
    }

    /**
     * 根据餐次类型过滤（关联 Recipe 表）
     *
     * @param mealType 餐次类型（BREAKFAST/LUNCH/DINNER）
     * @return 查询规格
     */
    public static Specification<UserHistory> byMealType(String mealType) {
        return (root, query, criteriaBuilder) -> {
            if (mealType == null || mealType.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            // 手动 JOIN Recipe 表（因为 UserHistory 中没有 @ManyToOne 关联）
            // 使用子查询方式实现
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Recipe> recipeRoot = subquery.from(Recipe.class);
            
            subquery.select(recipeRoot.get("id"))
                    .where(criteriaBuilder.equal(
                        recipeRoot.get("mealType"),
                        mealType.toUpperCase()
                    ));

            return criteriaBuilder.in(root.get("recipeId")).value(subquery);
        };
    }

    /**
     * 组合多个查询条件
     * <p>
     * 使用示例：
     * <pre>
     * Specification&lt;UserHistory&gt; spec = Specification
     *     .where(byUserId(userId))
     *     .and(byDishName(dishName))
     *     .and(byFeedbackAction(feedbackAction))
     *     .and(byMealType(mealType));
     * </pre>
     * </p>
     *
     * @param userId         用户ID（必填）
     * @param dishName       菜单名称（可选）
     * @param feedbackAction 反馈动作（可选）
     * @param mealType       餐次类型（可选）
     * @return 组合后的查询规格
     */
    public static Specification<UserHistory> buildSpecification(
            Long userId,
            String dishName,
            FeedbackAction feedbackAction,
            String mealType) {
        
        return Specification
                .where(byUserId(userId))
                .and(byDishName(dishName))
                .and(byFeedbackAction(feedbackAction))
                .and(byMealType(mealType));
    }
}

