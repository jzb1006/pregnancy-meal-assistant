package com.hjkj.pregnancy.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "胎动记录请求")
public class FetalMovementRequest {

    @Schema(description = "开始时间")
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(timezone = "GMT+8")
    private LocalDateTime startTime;

    @Schema(description = "持续时长(秒)")
    @NotNull(message = "持续时长不能为空")
    private Integer durationSeconds;

    @Schema(description = "胎动次数")
    @NotNull(message = "胎动次数不能为空")
    private Integer count;
}
