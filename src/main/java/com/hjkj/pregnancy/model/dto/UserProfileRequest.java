package com.hjkj.pregnancy.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hjkj.pregnancy.validation.LmpWithinRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * 用户档案请求DTO
 * <p>
 * 使用 Java 21 Record 实现不可变数据传输对象
 * </p>
 * 
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
@Schema(description = "用户档案请求对象")
public record UserProfileRequest(

        @NotBlank(message = "用户标识不能为空") @Schema(description = "用户唯一标识（微信OpenID）", example = "wx_123456") String openId,

        @NotNull(message = "末次月经日期不能为空") @PastOrPresent(message = "末次月经日期不能是未来时间") @LmpWithinRange(maxDays = 301) @JsonFormat(pattern = "yyyy-MM-dd") @Schema(description = "末次月经日期", example = "2025-10-01") LocalDate lmp,

        @NotNull(message = "身高不能为空") @Min(value = 100, message = "身高不能小于100cm") @Max(value = 250, message = "身高不能大于250cm") @Schema(description = "身高(cm)", example = "165") Integer height,

        @NotNull(message = "体重不能为空") @DecimalMin(value = "30.0", message = "体重不能小于30kg") @DecimalMax(value = "200.0", message = "体重不能大于200kg") @Schema(description = "当前体重(kg)", example = "58.5") Double weight,

        @NotNull(message = "出生日期不能为空") @Past(message = "出生日期必须是过去的时间") @JsonFormat(pattern = "yyyy-MM-dd") @Schema(description = "出生日期", example = "1990-05-15") LocalDate birthDate,

        @Schema(description = "饮食偏好（可选）", example = "CHINESE", allowableValues = {
                "CHINESE", "WESTERN", "JAPANESE_KOREAN", "SOUTHEAST_ASIAN", "VEGETARIAN",
                "NO_PREFERENCE" }) String cuisinePreference,

        @Schema(description = "过敏源（可选，部分逗号分隔）", example = "海鲜,花生") String allergies,

        @Schema(description = "忌口（可选，部分逗号分隔）", example = "香菜,辣") String dietaryRestrictions,

        @Schema(description = "饮食强偏好（可选，部分逗号分隔）", example = "酸,甜") String preferences){
}
