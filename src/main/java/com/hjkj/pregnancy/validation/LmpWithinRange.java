package com.hjkj.pregnancy.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 末次月经日期范围验证注解
 * <p>
 * 用于验证末次月经日期是否在合理的时间范围内（不超过指定的最大天数）。
 * 配合 {@link LmpWithinRangeValidator} 使用，支持自定义最大天数参数。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @LmpWithinRange(maxDays = 301, message = "末次月经日期需在0~301天内")
 * private LocalDate lmp;
 * }
 * </pre>
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LmpWithinRangeValidator.class)
public @interface LmpWithinRange {

    /**
     * 允许的最大天数（从末次月经到今天的最大间隔天数）
     * <p>
     * 默认值为 301 天（约 43 周），符合医学上对过期妊娠的定义。
     * </p>
     *
     * @return 最大允许天数
     */
    int maxDays() default 301;

    /**
     * 验证失败时的错误消息
     * <p>
     * 支持消息插值，可使用 {maxDays} 占位符。
     * </p>
     *
     * @return 错误消息模板
     */
    String message() default "末次月经日期需在0~{maxDays}天内（约43周），请检查后重试";

    /**
     * 验证分组
     *
     * @return 分组类数组
     */
    Class<?>[] groups() default {};

    /**
     * 验证负载
     *
     * @return 负载类数组
     */
    Class<? extends Payload>[] payload() default {};
}