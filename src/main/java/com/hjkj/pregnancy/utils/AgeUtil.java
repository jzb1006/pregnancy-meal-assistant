package com.hjkj.pregnancy.utils;

import com.hjkj.pregnancy.constants.PregnancyConstants;

import java.time.LocalDate;
import java.time.Period;

/**
 * 年龄计算工具类
 * <p>
 * 提供孕妇年龄相关的计算和营养建议功能，支持年龄分组和个性化营养指导
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2024-12-30
 */
public class AgeUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private AgeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 年龄分组枚举
     */
    public enum AgeGroup {
        /**
         * 低龄孕妇（<20岁）
         */
        YOUNG,

        /**
         * 适龄孕妇（20-34岁）
         */
        NORMAL,

        /**
         * 高龄孕妇（35-39岁）
         */
        SENIOR,

        /**
         * 超高龄孕妇（≥40岁）
         */
        SUPER_SENIOR
    }

    /**
     * 计算年龄
     * <p>
     * 根据出生日期计算当前年龄（周岁）
     * </p>
     *
     * @param birthDate 出生日期，不能为null
     * @return 年龄（周岁）
     * @throws IllegalArgumentException 如果出生日期为null或未来日期
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("出生日期不能为空");
        }

        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            throw new IllegalArgumentException("出生日期不能是未来时间");
        }

        return Period.between(birthDate, today).getYears();
    }

    /**
     * 获取年龄分组
     * <p>
     * 根据年龄判断所属分组：
     * <ul>
     *   <li>YOUNG: <20岁</li>
     *   <li>NORMAL: 20-34岁</li>
     *   <li>SENIOR: 35-39岁</li>
     *   <li>SUPER_SENIOR: ≥40岁</li>
     * </ul>
     * </p>
     *
     * @param age 年龄（周岁）
     * @return 年龄分组枚举
     * @throws IllegalArgumentException 如果年龄小于0或大于100
     */
    public static AgeGroup getAgeGroup(int age) {
        if (age < PregnancyConstants.Age.MIN_AGE || age > PregnancyConstants.Age.MAX_AGE) {
            throw new IllegalArgumentException("年龄必须在0-100之间");
        }

        if (age < PregnancyConstants.Age.YOUNG_MAX_AGE) {
            return AgeGroup.YOUNG;
        } else if (age < PregnancyConstants.Age.NORMAL_MAX_AGE) {
            return AgeGroup.NORMAL;
        } else if (age < PregnancyConstants.Age.SENIOR_MAX_AGE) {
            return AgeGroup.SENIOR;
        } else {
            return AgeGroup.SUPER_SENIOR;
        }
    }

    /**
     * 获取年龄分组的中文标签
     * <p>
     * 将年龄分组枚举转换为用户友好的中文描述
     * </p>
     *
     * @param ageGroup 年龄分组枚举
     * @return 中文标签
     */
    public static String getAgeGroupLabel(AgeGroup ageGroup) {
        return switch (ageGroup) {
            case YOUNG -> "低龄孕妇";
            case NORMAL -> "适龄孕妇";
            case SENIOR -> "高龄孕妇";
            case SUPER_SENIOR -> "超高龄孕妇";
        };
    }

    /**
     * 根据年龄获取营养建议
     * <p>
     * 为不同年龄段的孕妇提供个性化的营养指导建议
     * </p>
     *
     * @param age 年龄（周岁）
     * @return 营养建议文本
     */
    public static String getNutritionAdvice(int age) {
        AgeGroup group = getAgeGroup(age);

        return switch (group) {
            case YOUNG -> """
                身体仍在发育期，需要额外营养支持自身和胎儿双重发育需求。
                重点补充：优质蛋白质、钙质（每日1200mg）、铁（每日30mg）、叶酸。
                建议热量摄入比标准增加10-15%，多食用奶制品、瘦肉、深绿色蔬菜。
                """;

            case NORMAL -> """
                身体机能处于最佳状态，遵循标准孕期营养方案即可。
                重点关注：营养均衡、适量运动、体重管理。
                建议每日摄入：蛋白质70-85g、钙1000mg、铁27mg、叶酸600μg。
                """;

            case SENIOR -> """
                新陈代谢开始下降，妊娠糖尿病和高血压风险增加。
                重点关注：控制总热量（减少10-15%）、低GI饮食、优质蛋白。
                建议增加：钙质（1200mg/天）、叶酸（800μg/天）、DHA（200mg/天）。
                避免高糖、高盐、高脂食物，少食多餐，监测血糖血压。
                """;

            case SUPER_SENIOR -> """
                妊娠并发症风险显著增高，需要严格的营养管理和医学监护。
                饮食原则：低GI、低盐（<5g/天）、低脂、高蛋白、高纤维。
                重点补充：叶酸（800-1000μg/天）、钙（1200mg/天）、DHA、抗氧化营养素。
                建议清淡烹饪、易消化食材、少食多餐（5-6餐/天）。
                必须定期产检，密切监测血压、血糖、胎儿发育指标。
                """;
        };
    }

    /**
     * 获取年龄相关的饮食关键词
     * <p>
     * 为AI推荐提供年龄相关的食材和烹饪方式关键词
     * </p>
     *
     * @param age 年龄（周岁）
     * @return 饮食关键词（逗号分隔）
     */
    public static String getDietKeywords(int age) {
        AgeGroup group = getAgeGroup(age);

        return switch (group) {
            case YOUNG -> "高钙,高蛋白,高铁,营养密集,易吸收";
            case NORMAL -> "营养均衡,品类丰富,新鲜食材,适量热量";
            case SENIOR -> "低GI,高纤维,优质蛋白,适量脂肪,控制热量";
            case SUPER_SENIOR -> "低盐,低糖,低脂,易消化,清淡,抗氧化";
        };
    }
}

