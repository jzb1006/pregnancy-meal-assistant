package com.hjkj.pregnancy.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 微信小程序登录请求
 * <p>
 * 用户在小程序端调用wx.login()获取临时登录凭证code，
 * 然后将code发送到后端进行登录验证。
 * </p>
 *
 * @param code 微信小程序登录凭证code，通过wx.login()获取
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Schema(description = "微信小程序登录请求")
public record LoginRequest(
        @Schema(description = "微信小程序登录凭证code", example = "081abc123XYZ", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "登录凭证code不能为空")
        String code
) {
}

