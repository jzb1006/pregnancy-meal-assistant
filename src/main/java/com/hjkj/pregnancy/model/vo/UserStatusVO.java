package com.hjkj.pregnancy.model.vo;

import com.hjkj.pregnancy.constants.PregnancyConstants;
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

    @Schema(description = "从末次月经起累计天数（用于精确判断超预产期）", example = "281")
    private Integer pregnancyDays;

    /**
     * 获取状态提示
     * <p>
     * 当孕期天数超过280天（40周）时，返回超预产期健康提示。
     * 此方法动态计算，不持久化，提示内容随孕期进展自动更新。
     * 使用 PregnancyConstants 中的常量避免魔法数字。
     * </p>
     *
     * @return 状态提示，超过预产期天数时返回"已超预产期，请及时就医"，否则返回 null
     */
    @Schema(description = "状态提示（超预产期提醒）", example = "已超预产期，请及时就医")
    public String getStatusTip() {
        return pregnancyDays != null && pregnancyDays > PregnancyConstants.PregnancyStage.DUE_DATE_DAYS
            ? "已超预产期，请及时就医"
            : null;
    }
}

