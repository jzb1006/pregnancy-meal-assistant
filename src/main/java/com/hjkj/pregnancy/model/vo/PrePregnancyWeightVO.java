package com.hjkj.pregnancy.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 孕前体重响应
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "孕前体重信息")
public class PrePregnancyWeightVO {

    @Schema(description = "体重(kg)", example = "50.0")
    private BigDecimal weight;

    @Schema(description = "数据来源", example = "user_profile")
    private String source;
}

