package com.hjkj.pregnancy.enums;

import com.hjkj.pregnancy.constants.PregnancyConstants;

/**
 * 孕期阶段枚举
 * <p>
 * 根据孕周划分孕期的不同阶段
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public enum PregnancyStage {
    /**
     * 孕早期（1-12周）
     */
    EARLY("孕早期", 1, PregnancyConstants.PregnancyStage.EARLY_END_WEEK),

    /**
     * 孕中期（13-28周）
     */
    MIDDLE("孕中期", PregnancyConstants.PregnancyStage.EARLY_END_WEEK + 1, PregnancyConstants.PregnancyStage.MIDDLE_END_WEEK),

    /**
     * 孕晚期（29-40周）
     */
    LATE("孕晚期", PregnancyConstants.PregnancyStage.MIDDLE_END_WEEK + 1, PregnancyConstants.PregnancyStage.LATE_END_WEEK),

    /**
     * 已过预产期（>40周）
     */
    OVERDUE("已过预产期", PregnancyConstants.PregnancyStage.LATE_END_WEEK + 1, Integer.MAX_VALUE);

    private final String label;
    private final int startWeek;
    private final int endWeek;

    PregnancyStage(String label, int startWeek, int endWeek) {
        this.label = label;
        this.startWeek = startWeek;
        this.endWeek = endWeek;
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
     * 获取起始周数
     *
     * @return 起始周数
     */
    public int getStartWeek() {
        return startWeek;
    }

    /**
     * 获取结束周数
     *
     * @return 结束周数
     */
    public int getEndWeek() {
        return endWeek;
    }

    /**
     * 根据孕周获取孕期阶段
     * <p>
     * 特殊处理：
     * <ul>
     *   <li>week = 0：返回孕早期（刚刚开始怀孕）</li>
     *   <li>week < 0：抛出异常（不应该出现负数孕周）</li>
     *   <li>未匹配任何阶段：返回已过预产期</li>
     * </ul>
     * </p>
     *
     * @param week 孕周
     * @return 孕期阶段枚举
     * @throws IllegalArgumentException 如果孕周为负数
     */
    public static PregnancyStage fromWeek(int week) {
        if (week < 0) {
            throw new IllegalArgumentException("孕周不能为负数");
        }

        // 特殊处理：week = 0 时返回孕早期（刚刚开始怀孕）
        if (week == 0) {
            return EARLY;
        }

        for (PregnancyStage stage : values()) {
            if (week >= stage.startWeek && week <= stage.endWeek) {
                return stage;
            }
        }

        return OVERDUE;
    }
}

