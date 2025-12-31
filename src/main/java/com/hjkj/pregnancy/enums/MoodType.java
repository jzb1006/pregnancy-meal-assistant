package com.hjkj.pregnancy.enums;

/**
 * 用户心情类型枚举
 * <p>
 * 用于每日鼓励语录功能，记录用户当日心情状态。
 * AI 会根据不同心情类型生成针对性的鼓励文案。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
public enum MoodType {
    /**
     * 开心（默认值）
     */
    HAPPY("开心", "妈妈心情愉悦，宝宝也很开心"),

    /**
     * 疲惫
     */
    TIRED("疲惫", "妈妈感到疲惫，需要安慰和鼓励"),

    /**
     * 焦虑
     */
    ANXIOUS("焦虑", "妈妈有些焦虑，需要安抚和陪伴"),

    /**
     * 兴奋
     */
    EXCITED("兴奋", "妈妈对未来充满期待"),

    /**
     * 平静
     */
    CALM("平静", "妈妈心态平和，宝宝感到安心");

    private final String label;
    private final String description;

    MoodType(String label, String description) {
        this.label = label;
        this.description = description;
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
     * 获取描述信息
     *
     * @return 描述信息
     */
    public String getDescription() {
        return description;
    }
}