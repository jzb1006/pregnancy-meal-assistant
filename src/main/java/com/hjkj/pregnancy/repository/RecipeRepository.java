package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 食谱数据访问层
 * 
 * @author Zhibin Jiang
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /**
     * 智能匹配食谱（排除已浏览的食谱）
     * 
     * @param bmiCategory   BMI分类
     * @param mealType      餐次类型
     * @param pregnancyWeek 孕周
     * @param excludeIds    排除的食谱ID列表
     * @return 匹配的食谱
     */
    @Query("""
            SELECT r FROM Recipe r
            WHERE (r.bmiCategory = :bmiCategory OR r.bmiCategory = 'ALL')
            AND r.mealType = :mealType
            AND r.pregnancyWeek = :pregnancyWeek
            AND r.id NOT IN :excludeIds
            ORDER BY r.createdAt DESC
            """)
    Optional<Recipe> findSmartMatch(
            @Param("bmiCategory") String bmiCategory,
            @Param("mealType") String mealType,
            @Param("pregnancyWeek") Integer pregnancyWeek,
            @Param("excludeIds") List<Long> excludeIds);

    /**
     * 随机获取降级食谱 (Fallback)
     * 
     * @param bmiCategory BMI分类
     * @param mealType    餐次类型
     * @return 随机食谱
     */
    @Query(value = """
            SELECT * FROM recipe
            WHERE (bmi_category = :bmiCategory OR bmi_category = 'ALL')
            AND meal_type = :mealType
            ORDER BY RAND()
            LIMIT 1
            """, nativeQuery = true)
    Optional<Recipe> findRandomFallback(
            @Param("bmiCategory") String bmiCategory,
            @Param("mealType") String mealType);

    /**
     * 根据条件查询食谱列表
     * 
     * @param bmiCategory   BMI分类
     * @param mealType      餐次类型
     * @param pregnancyWeek 孕周
     * @return 食谱列表
     */
    List<Recipe> findByBmiCategoryAndMealTypeAndPregnancyWeek(
            String bmiCategory,
            String mealType,
            Integer pregnancyWeek);
}
