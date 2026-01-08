package com.hjkj.pregnancy.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 产检时光轴响应
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "产检时光轴")
public class PrenatalCheckTimelineVO {

    @Schema(description = "当前孕周", example = "16")
    private Integer currentWeek;

    @Schema(description = "产检分组列表")
    private List<PrenatalCheckGroupVO> groups;
}

