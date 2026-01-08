package com.hjkj.pregnancy.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 体重记录响应
 * 
 * @author Zhibin Jiang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "体重记录")
public class WeightRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "记录日期", example = "2025-01-07")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Schema(description = "体重(kg)", example = "57.5")
    private BigDecimal weight;

    @Schema(description = "孕周", example = "16")
    private Integer week;

    @Schema(description = "备注", example = "早餐后测量")
    private String note;
}

