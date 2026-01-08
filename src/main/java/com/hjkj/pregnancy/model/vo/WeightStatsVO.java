package com.hjkj.pregnancy.model.vo;

import com.hjkj.pregnancy.enums.BmiCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 体重统计信息
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "体重统计信息")
public class WeightStatsVO {

    @Schema(description = "当前体重(kg)", example = "57.5")
    private BigDecimal currentWeight;

    @Schema(description = "孕前体重(kg)", example = "50.0")
    private BigDecimal prePregnancyWeight;

    @Schema(description = "体重增量(kg)", example = "7.5")
    private BigDecimal weightGain;

    @Schema(description = "当前孕周", example = "16")
    private Integer currentWeek;

    @Schema(description = "BMI指数", example = "22.5")
    private BigDecimal bmi;

    @Schema(description = "BMI分类", example = "NORMAL")
    private BmiCategory bmiCategory;
}

