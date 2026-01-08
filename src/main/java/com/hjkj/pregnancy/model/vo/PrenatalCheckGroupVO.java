package com.hjkj.pregnancy.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 产检分组响应
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "产检分组")
public class PrenatalCheckGroupVO {

    @Schema(description = "阶段", example = "EARLY")
    private String stage;

    @Schema(description = "阶段标题", example = "孕早期 (1-13周)")
    private String title;

    @Schema(description = "阶段图标", example = "🌱")
    private String icon;

    @Schema(description = "产检项目列表")
    private List<PrenatalCheckItemVO> items;
}

