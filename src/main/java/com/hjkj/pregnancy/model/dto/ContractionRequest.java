package com.hjkj.pregnancy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "宫缩记录请求")
public class ContractionRequest {

    @Schema(description = "开始时间")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "持续时长(秒)")
    @NotNull(message = "持续时长不能为空")
    private Integer durationSeconds;

    @Schema(description = "疼痛等级(1-10)")
    private Integer painLevel;
}
