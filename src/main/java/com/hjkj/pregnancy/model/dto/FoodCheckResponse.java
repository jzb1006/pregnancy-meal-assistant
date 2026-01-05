package com.hjkj.pregnancy.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodCheckResponse {

    @JsonProperty("food_name")
    private String foodName;

    @JsonProperty("safety_level")
    private String safetyLevel; // RED, YELLOW, GREEN

    @JsonProperty("short_conclusion")
    private String shortConclusion;

    @JsonProperty("risk_analysis")
    private String riskAnalysis;

    @JsonProperty("suggested_amount")
    private String suggestedAmount;
}
