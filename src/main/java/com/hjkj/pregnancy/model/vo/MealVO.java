package com.hjkj.pregnancy.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 食谱推荐响应VO
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "食谱推荐信息")
public class MealVO {

    @Schema(description = "食谱ID", example = "101")
    private Long id;

    @Schema(description = "菜品名称", example = "彩椒炒牛肉粒")
    private String dishName;

    @Schema(description = "推荐理由", example = "针对孕中期且BMI微胖的你，这道菜高蛋白低脂...")
    private String reason;

    @Schema(description = "标签列表", example = "[\"补铁\", \"控糖\"]")
    private List<String> tags;

    @Schema(description = "安全等级", example = "GREEN")
    private String safety;

    @Schema(description = "烹饪时间", example = "10分钟")
    private String cookTime;

    @Schema(description = "食材列表", example = "[\"牛肉 200g\", \"彩椒 1个\"]")
    private List<String> ingredients;

    @Schema(description = "烹饪步骤", example = "[\"切粒\", \"腌制\", \"快炒\"]")
    private List<String> steps;

    @Schema(description = "准爸爸任务", example = "准爸爸负责切彩椒，并负责洗碗。")
    private String husbandTask;

    @Schema(description = "营养成分")
    private NutritionInfo nutrition;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "营养成分信息")
    public static class NutritionInfo {
        @Schema(description = "热量(kcal)", example = "350")
        private Integer calories;

        @Schema(description = "蛋白质(g)", example = "25")
        private Double protein;

        @Schema(description = "脂肪(g)", example = "12")
        private Double fat;

        @Schema(description = "碳水化合物(g)", example = "30")
        private Double carbohydrate;
    }
}

