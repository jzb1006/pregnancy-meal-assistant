package com.hjkj.pregnancy.utils;

import com.hjkj.pregnancy.constants.PregnancyConstants;
import com.hjkj.pregnancy.enums.BmiCategory;

/**
 * BMI 计算工具类
 * 
 * @author Zhibin Jiang
 */
public class BmiUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private BmiUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 计算BMI指数
     * 
     * @param height 身高(cm)
     * @param weight 体重(kg)
     * @return BMI指数
     */
    public static double calculateBmi(int height, double weight) {
        if (height <= 0 || weight <= 0) {
            throw new IllegalArgumentException("身高和体重必须大于0");
        }
        
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }

    /**
     * 获取BMI分类
     * 
     * @param bmi BMI指数
     * @return BMI分类字符串（为保持向后兼容）
     */
    public static String getBmiCategory(double bmi) {
        return BmiCategory.fromBmi(bmi).name();
    }

    /**
     * 获取BMI分类枚举
     * 
     * @param bmi BMI指数
     * @return BMI分类枚举
     */
    public static BmiCategory getBmiCategoryEnum(double bmi) {
        return BmiCategory.fromBmi(bmi);
    }

    /**
     * 获取BMI描述
     * 
     * @param bmi BMI指数
     * @return BMI描述
     */
    public static String getBmiDescription(double bmi) {
        return BmiCategory.fromBmi(bmi).getDescription();
    }

    /**
     * 获取BMI对应的饮食建议
     * 
     * @param bmi BMI指数
     * @return 饮食建议
     */
    public static String getDietAdvice(double bmi) {
        return BmiCategory.fromBmi(bmi).getDietAdvice();
    }

    /**
     * 格式化BMI值（保留一位小数）
     * 
     * @param bmi BMI值
     * @return 格式化后的字符串
     */
    public static String formatBmi(double bmi) {
        return String.format("%.1f", bmi);
    }
}


