package com.hjkj.pregnancy.config;

import com.hjkj.pregnancy.exception.AiServiceException;
import com.hjkj.pregnancy.exception.BusinessException;
import com.hjkj.pregnancy.exception.ErrorCode;
import com.hjkj.pregnancy.exception.UserNotFoundException;
import com.hjkj.pregnancy.exception.ValidationException;
import com.hjkj.pregnancy.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理系统中的各种异常，返回标准化的错误响应
 * </p>
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理用户不存在异常
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        log.warn("用户不存在异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理AI服务异常
     */
    @ExceptionHandler(AiServiceException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<?> handleAiServiceException(AiServiceException e, HttpServletRequest request) {
        log.error("AI服务异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理数据验证异常
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.warn("数据验证异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - 路径: {}, 错误码: {}, 消息: {}", 
            request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     * <p>
     * 针对末次月经日期（lmp）字段提供特殊处理：
     * <ul>
     *   <li>使用专用错误码 {@link ErrorCode#INVALID_LMP_DATE}</li>
     *   <li>返回友好的错误消息，不拼接字段名前缀</li>
     *   <li>便于前端进行针对性的错误提示和引导</li>
     * </ul>
     * 其他字段保持现有格式，确保向后兼容性。
     * </p>
     *
     * @param e       方法参数验证异常
     * @param request HTTP 请求
     * @return 统一的错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        // 特殊处理 lmp 字段错误（友好提示 + 专用错误码）
        FieldError lmpError = e.getBindingResult().getFieldErrors().stream()
            .filter(err -> "lmp".equals(err.getField()))
            .findFirst()
            .orElse(null);

        if (lmpError != null) {
            String message = lmpError.getDefaultMessage();
            log.warn("末次月经日期验证失败 - 路径: {}, 错误: {}", request.getRequestURI(), message);
            return Result.error(ErrorCode.INVALID_LMP_DATE.getCode(), message);
        }

        // 其他字段保持现有格式（兼容现有前端）
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));

        log.warn("参数校验失败 - 路径: {}, 错误: {}", request.getRequestURI(), message);
        return Result.validateError(message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        
        log.warn("参数绑定失败 - 路径: {}, 错误: {}", request.getRequestURI(), message);
        return Result.validateError(message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 - 路径: {}, 时间: {}", 
            request.getRequestURI(), LocalDateTime.now(), e);
        return Result.error("系统繁忙，请稍后重试");
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - 路径: {}, 时间: {}", 
            request.getRequestURI(), LocalDateTime.now(), e);
        return Result.error("系统繁忙，请稍后重试");
    }
}


