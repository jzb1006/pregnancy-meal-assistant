package com.hjkj.pregnancy.enums;

/**
 * 餐次类型枚举
 * <p>
 * 定义一日三餐的类型，用于食谱推荐和分类
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public enum MealType {
    /**
     * 早餐
     */
    BREAKFAST("早餐"),

    /**
     * 午餐
     */
    LUNCH("午餐"),

    /**
     * 晚餐
     */
    DINNER("晚餐");

    private final String label;

    MealType(String label) {
        this.label = label;
    }

    /**
     * 获取中文标签
     *
     * @return 中文标签
     */
    public String getLabel() {
        return label;
    }

    /**
     * 从字符串转换为枚举
     *
     * @param value 字符串值
     * @return MealType枚举，如果无效则返回null
     */
    public static MealType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return MealType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 验证字符串是否为有效的餐次类型
     *
     * @param value 字符串值
     * @return 是否有效
     */
    public static boolean isValid(String value) {
        return fromString(value) != null;
    }
}





