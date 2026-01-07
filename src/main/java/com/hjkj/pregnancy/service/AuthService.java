package com.hjkj.pregnancy.service;

import com.hjkj.pregnancy.model.vo.LoginResponse;

/**
 * 认证服务接口
 * <p>
 * 处理用户认证相关的业务逻辑，包括微信小程序登录、token生成等。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
public interface AuthService {

    /**
     * 微信小程序登录
     * <p>
     * 通过微信登录凭证code获取用户的openId和session_key，
     * 然后生成JWT token并返回。如果用户已注册档案，同时返回用户信息。
     * </p>
     *
     * @param code 微信小程序登录凭证，通过wx.login()获取
     * @return 登录响应，包含openId、token、是否新用户、用户信息等
     * @throws com.hjkj.pregnancy.exception.AuthException 如果登录失败
     */
    LoginResponse wxLogin(String code);
}


