package com.hjkj.pregnancy.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信小程序登录响应
 * <p>
 * 包含用户的openId、JWT token以及用户状态信息。
 * 前端应将token保存到本地，后续请求时放入请求头Authorization中。
 * </p>
 * <p>
 * 字段说明：
 * <ul>
 *   <li>openId - 微信用户唯一标识，用于业务关联</li>
 *   <li>token - JWT token，用于身份验证</li>
 *   <li>isNewUser - 是否新用户，true表示需要引导用户完善档案</li>
 *   <li>userInfo - 用户档案信息，仅已注册用户返回</li>
 * </ul>
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "微信小程序登录响应")
public class LoginResponse {

    /**
     * 微信用户唯一标识（OpenID）
     */
    @Schema(description = "微信用户唯一标识", example = "oABC123xyz456")
    private String openId;

    /**
     * JWT token，后续请求需携带此token
     */
    @Schema(description = "JWT token，放入请求头Authorization: Bearer {token}", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /**
     * 是否新用户（未注册档案）
     */
    @Schema(description = "是否新用户，true表示需要引导完善档案", example = "true")
    private Boolean isNewUser;

    /**
     * 用户档案信息（仅已注册用户返回）
     */
    @Schema(description = "用户档案信息（仅已注册用户返回）")
    private UserProfileVO userInfo;
}


