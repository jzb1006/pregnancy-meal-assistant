package com.hjkj.pregnancy.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 体重记录保存请求
 * 
 * @author Zhibin Jiang
 */
@Data
@Schema(description = "体重记录保存请求")
public class WeightRecordRequest {

    @NotNull(message = "记录日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "记录日期", example = "2025-01-07", required = true)
    private LocalDate date;

    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "20.0", message = "体重不能小于20kg")
    @DecimalMax(value = "200.0", message = "体重不能大于200kg")
    @Schema(description = "体重(kg)", example = "57.5", required = true)
    private BigDecimal weight;

    @Size(max = 500, message = "备注不能超过500字")
    @Schema(description = "备注", example = "早餐后测量")
    private String note;
}

