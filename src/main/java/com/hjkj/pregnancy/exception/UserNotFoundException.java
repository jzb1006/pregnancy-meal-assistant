package com.hjkj.pregnancy.exception;

/**
 * 用户不存在异常
 * <p>
 * 当查询的用户不存在时抛出此异常
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public class UserNotFoundException extends BusinessException {

    /**
     * 构造函数
     *
     * @param openId 用户标识
     */
    public UserNotFoundException(String openId) {
        super(ErrorCode.USER_NOT_FOUND, "用户不存在: " + openId);
    }

    /**
     * 构造函数（默认消息）
     */
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}

