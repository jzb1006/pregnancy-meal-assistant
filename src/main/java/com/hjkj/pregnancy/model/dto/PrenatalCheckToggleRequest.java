package com.hjkj.pregnancy.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 产检完成状态切换请求
 * 
 * @author Zhibin Jiang
 */
@Data
@Schema(description = "产检完成状态切换请求")
public class PrenatalCheckToggleRequest {

    @NotBlank(message = "产检项目编码不能为空")
    @Schema(description = "产检项目编码", example = "first-check", required = true)
    private String templateCode;

    @NotNull(message = "完成状态不能为空")
    @Schema(description = "是否完成", example = "true", required = true)
    private Boolean done;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "实际检查日期", example = "2025-01-07")
    private LocalDate checkDate;

    @Schema(description = "备注", example = "已完成检查")
    private String note;
}

