package com.hjkj.pregnancy.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户状态响应VO
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户状态信息")
public class UserStatusVO {

    @Schema(description = "当前孕周", example = "12")
    private Integer week;

    @Schema(description = "BMI指数", example = "21.5")
    private Double bmi;

    @Schema(description = "BMI分类描述", example = "标准")
    private String bmiDesc;

    @Schema(description = "孕期阶段", example = "孕早期")
    private String stage;

    @Schema(description = "当前年龄", example = "28")
    private Integer age;

    @Schema(description = "温馨提示", example = "宝宝现在像个柠檬，你保持得很棒！")
    private String tips;
}

