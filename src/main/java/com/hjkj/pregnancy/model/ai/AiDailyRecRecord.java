package com.hjkj.pregnancy.model.ai;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * AI每日推荐响应记录
 *
 * @param dishName       菜品名称
 * @param seasonalReason 推荐理由
 * @param nutritionTags  营养标签
 * @param ingredients    食材列表
 * @param cookingTip     烹饪贴士
 */
public record AiDailyRecRecord(
                @JsonProperty("dishName") @JsonAlias("dish_name") String dishName,
                @JsonProperty("reason") @JsonAlias("seasonal_reason") String seasonalReason,
                @JsonProperty("tags") @JsonAlias("nutrition_tags") List<String> nutritionTags,
                @JsonProperty("ingredients") List<String> ingredients,
                @JsonProperty("cookingTip") @JsonAlias("cooking_tip") String cookingTip,
                @JsonProperty("cookTime") @JsonAlias("cook_time") String cookTime,
                @JsonProperty("steps") List<String> steps,
                @JsonProperty("husbandTask") @JsonAlias("husband_task") String husbandTask,
                @JsonProperty("nutrition") Nutrition nutrition) {

        public record Nutrition(
                        @JsonProperty("calories") int calories,
                        @JsonProperty("protein") double protein,
                        @JsonProperty("fat") double fat,
                        @JsonProperty("carbohydrate") double carbohydrate) {
        }
}
