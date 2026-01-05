package com.hjkj.pregnancy.exception;

/**
 * AI服务异常
 * <p>
 * 当AI服务调用失败时抛出此异常
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public class AiServiceException extends BusinessException {

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public AiServiceException(String message) {
        super(ErrorCode.AI_SERVICE_ERROR, message);
    }

    /**
     * 构造函数（带原因）
     *
     * @param message 错误消息
     * @param cause   原因
     */
    public AiServiceException(String message, Throwable cause) {
        super(ErrorCode.AI_SERVICE_ERROR, message, cause);
    }

    /**
     * 构造函数（默认消息）
     */
    public AiServiceException() {
        super(ErrorCode.AI_SERVICE_ERROR);
    }

    /**
     * 构造函数（默认消息带原因）
     *
     * @param cause 原因
     */
    public AiServiceException(Throwable cause) {
        super(ErrorCode.AI_SERVICE_ERROR, cause);
    }
}



