package com.hjkj.pregnancy.exception;

/**
 * 数据验证异常
 * <p>
 * 当数据验证失败时抛出此异常
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public class ValidationException extends BusinessException {

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    /**
     * 构造函数（默认消息）
     */
    public ValidationException() {
        super(ErrorCode.VALIDATION_ERROR);
    }
}

