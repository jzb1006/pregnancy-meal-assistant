package com.hjkj.pregnancy.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下次产检响应
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "下次产检信息")
public class NextPrenatalCheckVO {

    @Schema(description = "项目编码", example = "nt-check")
    private String id;

    @Schema(description = "孕周范围", example = "11-13")
    private String week;

    @Schema(description = "产检名称", example = "NT检查")
    private String title;

    @Schema(description = "是否完成")
    private Boolean done;

    @Schema(description = "距离建议检查时间还有多少天", example = "5")
    private Integer daysUntil;
}

