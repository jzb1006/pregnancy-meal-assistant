package com.hjkj.pregnancy.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户档案请求DTO
 * 
 * @author Zhibin Jiang
 */
@Data
@Schema(description = "用户档案请求对象")
public class UserProfileRequest {

    @NotBlank(message = "用户标识不能为空")
    @Schema(description = "用户唯一标识（微信OpenID）", example = "wx_123456")
    private String openId;

    @NotNull(message = "末次月经日期不能为空")
    @PastOrPresent(message = "末次月经日期不能是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "末次月经日期", example = "2025-10-01")
    private LocalDate lmp;

    @NotNull(message = "身高不能为空")
    @Min(value = 100, message = "身高不能小于100cm")
    @Max(value = 250, message = "身高不能大于250cm")
    @Schema(description = "身高(cm)", example = "165")
    private Integer height;

    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "30.0", message = "体重不能小于30kg")
    @DecimalMax(value = "200.0", message = "体重不能大于200kg")
    @Schema(description = "当前体重(kg)", example = "58.5")
    private Double weight;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "出生日期", example = "1990-05-15")
    private LocalDate birthDate;
}

