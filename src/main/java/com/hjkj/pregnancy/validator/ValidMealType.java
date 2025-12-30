package com.hjkj.pregnancy.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 餐次类型验证注解
 * <p>
 * 验证字符串是否为有效的餐次类型（BREAKFAST、LUNCH、DINNER）
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MealTypeValidator.class)
@Documented
public @interface ValidMealType {
    
    /**
     * 错误消息
     */
    String message() default "餐次类型无效，仅支持：BREAKFAST、LUNCH、DINNER";
    
    /**
     * 验证分组
     */
    Class<?>[] groups() default {};
    
    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}

