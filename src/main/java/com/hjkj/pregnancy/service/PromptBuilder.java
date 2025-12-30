package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.utils.BmiUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI Prompt 构建器
 * <p>
 * 使用Builder模式构建AI推荐食谱的Prompt，支持链式调用和参数验证
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public class PromptBuilder {

    private final StringBuilder prompt;
    
    private PromptBuilder() {
        this.prompt = new StringBuilder(2048); // 预分配容量
    }

    /**
     * 创建Prompt构建器
     *
     * @return PromptBuilder实例
     */
    public static PromptBuilder create() {
        return new PromptBuilder();
    }

    /**
     * 构建完整的AI Prompt
     *
     * @param context Prompt上下文
     * @return 完整的Prompt字符串
     */
    public static String buildMealRecommendationPrompt(PromptContext context) {
        PromptBuilder builder = create();
        
        String mealTypeCn = getMealTypeChinese(context.mealType);
        String bmiAdvice = BmiUtil.getDietAdvice(context.bmi);
        
        // 系统角色和任务描述
        builder.appendLine("你是一位专业的孕期营养师，请为孕妇推荐一道%s菜谱。", mealTypeCn)
               .appendLine();
        
        // 用户信息
        builder.appendLine("用户信息：")
               .appendLine("- 当前孕周：第%d周", context.week)
               .appendLine("- 孕期阶段：%s", context.stage)
               .appendLine("- BMI分类：%s (%.1f)", context.bmiCategory, context.bmi)
               .appendLine("- BMI饮食建议：%s", bmiAdvice)
               .appendLine("- 当前年龄：%d岁", context.age)
               .appendLine("- 年龄分组：%s", context.ageGroupLabel)
               .appendLine("- 年龄营养建议：%s", context.ageAdvice)
               .appendLine("- 年龄饮食关键词：%s", context.ageKeywords);
        
        // 饮食偏好
        if (context.cuisinePreference != null && !context.cuisinePreference.isBlank() 
            && !"无偏好".equals(context.cuisinePreference)) {
            builder.appendLine()
                   .appendLine("**饮食偏好：** 用户偏好%s风格菜品，请优先推荐相应菜系。", context.cuisinePreference);
        }
        
        // 历史菜品信息
        if (context.recentDishNames != null && !context.recentDishNames.isEmpty()) {
            String avoidKeywords = extractKeywords(context.recentDishNames);
            builder.appendLine()
                   .appendLine("**避免重复：** 用户最近看过：%s", avoidKeywords)
                   .appendLine("**多样性要求：** 主食材、烹饪方式、配菜需完全不同，尝试新风味。");
        }
        
        builder.appendLine();
        
        // 要求列表
        builder.appendLine("要求：")
               .appendLine("1. 菜品要符合孕妇营养需求，食材新鲜易得")
               .appendLine("2. 烹饪时间控制在30分钟以内")
               .appendLine("3. 必须标注食材安全等级（GREEN/YELLOW/RED）")
               .appendLine("4. 提供详细的食材用量和烹饪步骤")
               .appendLine("5. 包含准确的营养成分信息")
               .appendLine("6. 给准爸爸安排一个帮忙的小任务")
               .appendLine("7. 根据BMI调整菜品热量和营养配比")
               .appendLine("8. **重要：根据年龄分组和营养建议优化菜品，年龄是核心考虑因素**")
               .appendLine("   - 低龄孕妇：增加高钙、高蛋白、高铁食材")
               .appendLine("   - 适龄孕妇：营养均衡，品类丰富")
               .appendLine("   - 高龄孕妇：低GI、高纤维、控制热量")
               .appendLine("   - 超高龄孕妇：低盐、低糖、易消化、抗氧化")
               .appendLine("9. **创新性：每次推荐都要有创意，尝试不同的食材组合、烹饪技法和风味搭配**")
               .appendLine("10. **多样性：优先推荐用户从未见过的菜品类型，给用户新鲜感**")
               .appendLine()
               .appendLine("请以JSON格式返回，包含以下字段：")
               .appendLine("{")
               .appendLine("  \"dish_name\": \"菜品名称\",")
               .appendLine("  \"reason\": \"推荐理由（100字以内，需体现年龄因素）\",")
               .appendLine("  \"tags\": [\"标签1\", \"标签2\"],")
               .appendLine("  \"safety\": \"GREEN\",")
               .appendLine("  \"cook_time\": \"15分钟\",")
               .appendLine("  \"ingredients\": [\"食材1 用量\", \"食材2 用量\"],")
               .appendLine("  \"steps\": [\"步骤1\", \"步骤2\"],")
               .appendLine("  \"husband_task\": \"准爸爸的任务\",")
               .appendLine("  \"nutrition\": {")
               .appendLine("    \"calories\": 350,")
               .appendLine("    \"protein\": 25.0,")
               .appendLine("    \"fat\": 12.0,")
               .appendLine("    \"carbohydrate\": 30.0")
               .appendLine("  }")
               .appendLine("}");
        
        return builder.build();
    }

    /**
     * 追加一行文本（带换行）
     */
    private PromptBuilder appendLine(String format, Object... args) {
        if (args.length > 0) {
            prompt.append(String.format(format, args));
        } else {
            prompt.append(format);
        }
        prompt.append("\n");
        return this;
    }

    /**
     * 追加空行
     */
    private PromptBuilder appendLine() {
        prompt.append("\n");
        return this;
    }

    /**
     * 构建最终的Prompt字符串
     */
    private String build() {
        return prompt.toString();
    }

    /**
     * 提取菜品关键词（主食材和烹饪方式）
     */
    private static String extractKeywords(List<String> dishNames) {
        if (dishNames == null || dishNames.isEmpty()) {
            return "";
        }

        // 常见烹饪方式
        String[] cookingMethods = {"清蒸", "红烧", "香煎", "爆炒", "炖", "煮", "烤", "煎", "炒", "焖"};
        
        // 常见食材
        String[] mainIngredients = {
            "三文鱼", "鸡胸肉", "牛肉", "猪肉", "虾", "鱼", "豆腐", "鸡蛋",
            "鸡", "羊肉", "鳕鱼", "排骨", "鸭肉"
        };

        StringBuilder keywords = new StringBuilder();
        for (String dishName : dishNames) {
            // 提取烹饪方式
            for (String method : cookingMethods) {
                if (dishName.contains(method)) {
                    keywords.append(method).append("、");
                    break;
                }
            }

            // 提取主食材
            for (String ingredient : mainIngredients) {
                if (dishName.contains(ingredient)) {
                    keywords.append(ingredient).append("、");
                    break;
                }
            }
        }

        // 去除最后的分隔符
        String result = keywords.toString();
        if (result.endsWith("、")) {
            result = result.substring(0, result.length() - 1);
        }

        // 如果提取失败，直接返回前3个菜名
        if (result.isEmpty()) {
            return String.join("、", dishNames.subList(0, Math.min(3, dishNames.size())));
        }

        return result;
    }

    /**
     * 获取餐次类型的中文名称
     * <p>
     * 使用 Java 21 增强型 switch 表达式，支持模式匹配和空值处理
     * </p>
     */
    private static String getMealTypeChinese(String mealType) {
        // 使用 switch 表达式的模式匹配和空值处理
        return switch (mealType) {
            case null -> "餐食"; // Java 21 支持 null case
            case String s when s.equalsIgnoreCase("BREAKFAST") -> "早餐";
            case String s when s.equalsIgnoreCase("LUNCH") -> "午餐";
            case String s when s.equalsIgnoreCase("DINNER") -> "晚餐";
            default -> "餐食";
        };
    }

    /**
     * Prompt上下文
     * 包含构建Prompt所需的所有参数
     */
    @Getter
    @Builder
    public static class PromptContext {
        private final int week;
        private final String stage;
        private final String bmiCategory;
        private final double bmi;
        private final String mealType;
        private final int age;
        private final String ageGroupLabel;
        private final String ageAdvice;
        private final String ageKeywords;
        private final List<String> recentDishNames;
        private final String cuisinePreference;
    }
}

