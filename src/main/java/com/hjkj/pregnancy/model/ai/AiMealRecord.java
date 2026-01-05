package com.hjkj.pregnancy.model.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AI 生成的食谱数据结构
 * 使用 Record 类型，适配 Spring AI 的返回对象
 * 
 * @author Zhibin Jiang
 */
public record AiMealRecord(
    @JsonProperty("dish_name")
    String dishName,
    
    @JsonProperty("reason")
    String reason,
    
    @JsonProperty("tags")
    List<String> tags,
    
    @JsonProperty("safety")
    String safety,
    
    @JsonProperty("cook_time")
    String cookTime,
    
    @JsonProperty("ingredients")
    List<String> ingredients,
    
    @JsonProperty("steps")
    List<String> steps,
    
    @JsonProperty("husband_task")
    String husbandTask,
    
    @JsonProperty("nutrition")
    NutritionRecord nutrition
) {
    
    public record NutritionRecord(
        @JsonProperty("calories")
        Integer calories,
        
        @JsonProperty("protein")
        Double protein,
        
        @JsonProperty("fat")
        Double fat,
        
        @JsonProperty("carbohydrate")
        Double carbohydrate
    ) {}
}




