package com.hjkj.pregnancy.constants;

/**
 * 孕期相关常量定义
 * <p>
 * 集中管理项目中的所有常量值，便于维护和修改
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public final class PregnancyConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private PregnancyConstants() {
        throw new UnsupportedOperationException("Constants class");
    }

    /**
     * BMI相关常量
     */
    public static final class Bmi {
        private Bmi() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** BMI偏瘦阈值 */
        public static final double UNDERWEIGHT_THRESHOLD = 18.5;

        /** BMI标准阈值 */
        public static final double NORMAL_THRESHOLD = 24.0;

        /** BMI微胖阈值 */
        public static final double OVERWEIGHT_THRESHOLD = 28.0;
    }

    /**
     * 孕期阶段相关常量
     */
    public static final class PregnancyStage {
        private PregnancyStage() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** 孕早期结束周数 */
        public static final int EARLY_END_WEEK = 12;

        /** 孕中期结束周数 */
        public static final int MIDDLE_END_WEEK = 28;

        /** 孕晚期结束周数（预产期） */
        public static final int LATE_END_WEEK = 40;

        /** 预产期天数（从末次月经开始计算） */
        public static final int DUE_DATE_DAYS = 280;

        /** 一周的天数 */
        public static final int DAYS_PER_WEEK = 7;
    }

    /**
     * 历史记录相关常量
     */
    public static final class History {
        private History() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** 默认查询最近浏览记录数量 */
        public static final int DEFAULT_RECENT_COUNT = 20;

        /** AI提示词中传递的历史菜品数量 */
        public static final int AI_HISTORY_COUNT = 5;
    }

    /**
     * 超时配置相关常量
     */
    public static final class Timeout {
        private Timeout() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** SSE连接超时时间（毫秒） */
        public static final long SSE_TIMEOUT_MILLIS = 5 * 60 * 1000L;
    }

    /**
     * 烹饪方式关键词
     */
    public static final class CookingMethods {
        private CookingMethods() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** 常见烹饪方式数组 */
        public static final String[] METHODS = {
            "清蒸", "红烧", "香煎", "爆炒", "炖", 
            "煮", "烤", "煎", "炒", "焖"
        };
    }

    /**
     * 主要食材关键词
     */
    public static final class MainIngredients {
        private MainIngredients() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** 常见主食材数组 */
        public static final String[] INGREDIENTS = {
            "三文鱼", "鸡胸肉", "牛肉", "猪肉", "虾", "鱼", 
            "豆腐", "鸡蛋", "鸡", "羊肉", "鳕鱼", "排骨", "鸭肉"
        };
    }

    /**
     * 年龄分组相关常量
     */
    public static final class Age {
        private Age() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** 低龄孕妇上限（不含） */
        public static final int YOUNG_MAX_AGE = 20;

        /** 适龄孕妇上限（不含） */
        public static final int NORMAL_MAX_AGE = 35;

        /** 高龄孕妇上限（不含） */
        public static final int SENIOR_MAX_AGE = 40;

        /** 最小年龄 */
        public static final int MIN_AGE = 0;

        /** 最大年龄 */
        public static final int MAX_AGE = 100;
    }

    /**
     * AI模型相关常量
     */
    public static final class AiModel {
        private AiModel() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** 默认模型名称 */
        public static final String DEFAULT_MODEL = "qwen3-max";

        /** 默认温度参数 */
        public static final double DEFAULT_TEMPERATURE = 0.7;

        /** 默认最大Token数 */
        public static final int DEFAULT_MAX_TOKENS = 2000;
    }

    /**
     * 餐次类型相关常量
     */
    public static final class MealTypeNames {
        private MealTypeNames() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** 早餐 */
        public static final String BREAKFAST = "BREAKFAST";

        /** 午餐 */
        public static final String LUNCH = "LUNCH";

        /** 晚餐 */
        public static final String DINNER = "DINNER";
    }

    /**
     * 提取关键词相关常量
     */
    public static final class Keywords {
        private Keywords() {
            throw new UnsupportedOperationException("Constants class");
        }

        /** 关键词分隔符 */
        public static final String SEPARATOR = "、";

        /** 提取失败时显示的最大菜名数量 */
        public static final int MAX_DISH_NAMES = 3;
    }
}

