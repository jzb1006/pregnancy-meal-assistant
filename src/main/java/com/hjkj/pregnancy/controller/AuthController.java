package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.model.dto.LoginRequest;
import com.hjkj.pregnancy.model.vo.LoginResponse;
import com.hjkj.pregnancy.service.AuthService;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * <p>
 * 提供用户认证相关的接口，包括微信小程序登录等。
 * 登录接口不需要token验证，其他需要登录的接口应添加@RequireLogin注解。
 * </p>
 * <p>
 * 接口列表：
 * <ul>
 *   <li>POST /v1/auth/wx/login - 微信小程序登录</li>
 * </ul>
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 微信小程序登录
     * <p>
     * 前端调用流程：
     * <ol>
     *   <li>小程序调用wx.login()获取临时登录凭证code</li>
     *   <li>将code发送到此接口</li>
     *   <li>后端返回openId和token</li>
     *   <li>前端保存token，后续请求放入请求头：Authorization: Bearer {token}</li>
     *   <li>如果isNewUser为true，引导用户完善档案信息</li>
     * </ol>
     * </p>
     * <p>
     * 响应说明：
     * <ul>
     *   <li>openId - 微信用户唯一标识，用于业务关联</li>
     *   <li>token - JWT token，有效期7天</li>
     *   <li>isNewUser - true表示新用户，需要引导完善档案</li>
     *   <li>userInfo - 用户档案信息，仅老用户返回</li>
     * </ul>
     * </p>
     *
     * @param request 登录请求，包含微信登录凭证code
     * @return 登录响应，包含openId、token、用户状态等信息
     */
    @PostMapping("/wx/login")
    @Operation(
            summary = "微信小程序登录", 
            description = "使用微信小程序登录凭证code进行登录，返回openId和JWT token。" +
                    "新用户需要引导完善档案信息。token有效期7天。"
    )
    public Result<LoginResponse> wxLogin(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("收到微信登录请求");
            LoginResponse response = authService.wxLogin(request.code());
            log.info("微信登录成功，openId: {}, isNewUser: {}", response.getOpenId(), response.getIsNewUser());
            return Result.success(response);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return Result.error(e.getMessage());
        }
    }
}


