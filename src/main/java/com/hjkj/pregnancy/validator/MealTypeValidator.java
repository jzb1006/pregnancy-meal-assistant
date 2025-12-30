package com.hjkj.pregnancy.validator;

import com.hjkj.pregnancy.enums.MealType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 餐次类型验证器
 * <p>
 * 实现餐次类型的验证逻辑
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public class MealTypeValidator implements ConstraintValidator<ValidMealType, String> {

    @Override
    public void initialize(ValidMealType constraintAnnotation) {
        // 初始化逻辑（如果需要）
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 值由 @NotNull 等注解处理
        if (value == null) {
            return true;
        }
        
        // 使用 MealType 枚举的验证方法
        return MealType.isValid(value);
    }
}

