package com.hjkj.pregnancy.utils;

/**
 * BMI 计算工具类
 * 
 * @author Zhibin Jiang
 */
public class BmiUtil {

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
     * @return BMI分类
     */
    public static String getBmiCategory(double bmi) {
        if (bmi < 18.5) {
            return "UNDERWEIGHT";
        } else if (bmi < 24.0) {
            return "NORMAL";
        } else if (bmi < 28.0) {
            return "OVERWEIGHT";
        } else {
            return "OBESE";
        }
    }

    /**
     * 获取BMI描述
     * 
     * @param bmi BMI指数
     * @return BMI描述
     */
    public static String getBmiDescription(double bmi) {
        String category = getBmiCategory(bmi);
        return switch (category) {
            case "UNDERWEIGHT" -> "偏瘦";
            case "NORMAL" -> "标准";
            case "OVERWEIGHT" -> "微胖";
            case "OBESE" -> "肥胖";
            default -> "未知";
        };
    }

    /**
     * 获取BMI对应的饮食建议
     * 
     * @param bmi BMI指数
     * @return 饮食建议
     */
    public static String getDietAdvice(double bmi) {
        String category = getBmiCategory(bmi);
        return switch (category) {
            case "UNDERWEIGHT" -> "建议适当增加营养，多吃优质蛋白和健康脂肪";
            case "NORMAL" -> "保持均衡饮食，营养充足但不过量";
            case "OVERWEIGHT" -> "注意控制总热量，选择低脂高蛋白食物";
            case "OBESE" -> "需要控制体重增长，少油少糖，多吃蔬菜";
            default -> "请咨询专业医生";
        };
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


