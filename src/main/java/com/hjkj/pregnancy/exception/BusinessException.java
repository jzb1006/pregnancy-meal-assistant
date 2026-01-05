package com.hjkj.pregnancy.exception;

/**
 * 业务异常基类
 * <p>
 * 所有业务异常都应继承此类
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final Object[] args;

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * 构造函数（带自定义消息）
     *
     * @param errorCode 错误码
     * @param message   自定义消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * 构造函数（带原因）
     *
     * @param errorCode 错误码
     * @param cause     原因
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * 构造函数（带自定义消息和原因）
     *
     * @param errorCode 错误码
     * @param message   自定义消息
     * @param cause     原因
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * 构造函数（带消息参数）
     *
     * @param errorCode 错误码
     * @param args      消息参数
     */
    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误码值
     *
     * @return 错误码值
     */
    public int getCode() {
        return errorCode.getCode();
    }

    /**
     * 获取消息参数
     *
     * @return 消息参数
     */
    public Object[] getArgs() {
        return args;
    }
}



