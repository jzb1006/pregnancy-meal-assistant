package com.hjkj.pregnancy.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 产检项目响应
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "产检项目")
public class PrenatalCheckItemVO {

    @Schema(description = "项目编码", example = "first-check")
    private String id;

    @Schema(description = "孕周范围", example = "6-8")
    private String week;

    @Schema(description = "产检名称", example = "首次产检")
    private String title;

    @Schema(description = "简短描述", example = "确认宫内孕、胎心胎芽")
    private String shortDesc;

    @Schema(description = "详细说明")
    private String details;

    @Schema(description = "注意事项")
    private String tips;

    @Schema(description = "是否完成")
    private Boolean done;

    @Schema(description = "实际检查日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkDate;

    @Schema(description = "是否为当前孕周")
    private Boolean isActive;
}

