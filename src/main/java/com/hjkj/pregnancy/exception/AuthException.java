package com.hjkj.pregnancy.exception;

/**
 * 认证异常
 * <p>
 * 用于处理用户认证相关的异常情况，如未登录、token无效、token过期等。
 * 继承自BusinessException，会被全局异常处理器统一处理。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
public class AuthException extends BusinessException {

    /**
     * 使用错误码构造异常
     *
     * @param errorCode 错误码枚举
     */
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 使用错误码和自定义消息构造异常
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}


