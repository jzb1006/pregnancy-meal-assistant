package com.hjkj.pregnancy.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 用户完整档案VO
 * 
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户完整档案信息")
public class UserProfileVO {

    @Schema(description = "用户唯一标识", example = "wx_123456")
    private String openId;

    @Schema(description = "末次月经日期", example = "2025-10-01")
    private LocalDate lmp;

    @Schema(description = "身高(cm)", example = "165")
    private Integer height;

    @Schema(description = "当前体重(kg)", example = "58.5")
    private Double weight;

    @Schema(description = "出生日期", example = "1990-05-15")
    private LocalDate birthDate;

    @Schema(description = "饮食偏好代码", example = "CHINESE")
    private String cuisinePreference;

    @Schema(description = "饮食偏好描述", example = "中餐")
    private String cuisinePreferenceLabel;

    @Schema(description = "过敏源", example = "海鲜,花生")
    private String allergies;

    @Schema(description = "忌口", example = "香菜,辣")
    private String dietaryRestrictions;

    @Schema(description = "饮食强偏好", example = "酸,甜")
    private String preferences;
}
