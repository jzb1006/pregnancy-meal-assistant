package com.hjkj.pregnancy.enums;

/**
 * 食品安全等级枚举
 * <p>
 * 用于标识食材对孕妇的安全程度
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public enum SafetyLevel {
    /**
     * 绿色 - 安全，可以放心食用
     */
    GREEN("安全", "可以放心食用"),

    /**
     * 黄色 - 谨慎，适量食用
     */
    YELLOW("谨慎", "适量食用，注意控制量"),

    /**
     * 红色 - 禁忌，不建议食用
     */
    RED("禁忌", "孕期不建议食用");

    private final String label;
    private final String description;

    SafetyLevel(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * 获取标签
     *
     * @return 标签
     */
    public String getLabel() {
        return label;
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
     * 从字符串转换为枚举
     *
     * @param value 字符串值
     * @return SafetyLevel枚举，如果无效则返回GREEN
     */
    public static SafetyLevel fromString(String value) {
        if (value == null || value.isBlank()) {
            return GREEN;
        }

        try {
            return SafetyLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GREEN;
        }
    }
}





