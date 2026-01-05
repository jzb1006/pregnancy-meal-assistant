package com.hjkj.pregnancy.enums;

import com.hjkj.pregnancy.constants.PregnancyConstants;

/**
 * BMI分类枚举
 * <p>
 * 根据BMI指数对孕妇体重进行分类，用于个性化饮食建议
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public enum BmiCategory {
    /**
     * 偏瘦（BMI < 18.5）
     */
    UNDERWEIGHT("偏瘦", "建议适当增加营养，多吃优质蛋白和健康脂肪"),

    /**
     * 标准（18.5 ≤ BMI < 24.0）
     */
    NORMAL("标准", "保持均衡饮食，营养充足但不过量"),

    /**
     * 微胖（24.0 ≤ BMI < 28.0）
     */
    OVERWEIGHT("微胖", "注意控制总热量，选择低脂高蛋白食物"),

    /**
     * 肥胖（BMI ≥ 28.0）
     */
    OBESE("肥胖", "需要控制体重增长，少油少糖，多吃蔬菜"),

    /**
     * 全部（用于食谱匹配时表示适用所有BMI分类）
     */
    ALL("全部", "适用所有体重分类");

    private final String description;
    private final String dietAdvice;

    BmiCategory(String description, String dietAdvice) {
        this.description = description;
        this.dietAdvice = dietAdvice;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取饮食建议
     *
     * @return 饮食建议
     */
    public String getDietAdvice() {
        return dietAdvice;
    }

    /**
     * 根据BMI值获取分类
     *
     * @param bmi BMI指数
     * @return BMI分类枚举
     */
    public static BmiCategory fromBmi(double bmi) {
        if (bmi < PregnancyConstants.Bmi.UNDERWEIGHT_THRESHOLD) {
            return UNDERWEIGHT;
        } else if (bmi < PregnancyConstants.Bmi.NORMAL_THRESHOLD) {
            return NORMAL;
        } else if (bmi < PregnancyConstants.Bmi.OVERWEIGHT_THRESHOLD) {
            return OVERWEIGHT;
        } else {
            return OBESE;
        }
    }

    /**
     * 从字符串转换为枚举
     *
     * @param value 字符串值
     * @return BmiCategory枚举，如果无效则返回null
     */
    public static BmiCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return BmiCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}



