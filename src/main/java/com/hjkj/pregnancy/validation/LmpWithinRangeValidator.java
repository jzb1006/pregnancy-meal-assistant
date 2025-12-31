package com.hjkj.pregnancy.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 末次月经日期范围验证器
 * <p>
 * 验证末次月经日期是否在合理的时间范围内。
 * 采用单一职责原则，仅验证日期是否超过最大允许天数，
 * 其他验证（如非空、未来日期）交由其他验证注解处理。
 * </p>
 * <p>
 * 验证逻辑：
 * <ul>
 *   <li>null 值：返回 true（必填验证由 @NotNull 处理）</li>
 *   <li>未来日期：返回 true（未来日期验证由 @PastOrPresent 处理）</li>
 *   <li>过早日期：计算距今天数，超过 maxDays 返回 false</li>
 * </ul>
 * </p>
 * <p>
 * 通过注入 Clock 实现时区统一和测试友好性。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@RequiredArgsConstructor
public class LmpWithinRangeValidator implements ConstraintValidator<LmpWithinRange, LocalDate> {

    /**
     * 全局时钟，用于获取当前日期
     * <p>
     * 通过依赖注入获取，确保时区统一为 Asia/Shanghai。
     * 测试时可通过 @Primary 注解提供 Clock.fixed(...) 实现稳定的单元测试。
     * </p>
     */
    private final Clock clock;

    /**
     * 允许的最大天数
     */
    private int maxDays;

    /**
     * 初始化验证器，获取注解参数
     *
     * @param annotation LmpWithinRange 注解实例
     */
    @Override
    public void initialize(LmpWithinRange annotation) {
        this.maxDays = annotation.maxDays();
    }

    /**
     * 执行验证逻辑
     * <p>
     * 验证策略：
     * <ul>
     *   <li>null 值：返回 true，让 @NotNull 处理必填验证</li>
     *   <li>未来日期：返回 true，让 @PastOrPresent 处理未来日期验证，避免错误消息冲突</li>
     *   <li>过早日期：计算从 lmp 到今天的天数，超过 maxDays 返回 false</li>
     * </ul>
     * </p>
     *
     * @param lmp     末次月经日期
     * @param context 验证上下文
     * @return 验证是否通过
     */
    @Override
    public boolean isValid(LocalDate lmp, ConstraintValidatorContext context) {
        // null 值交由 @NotNull 验证
        if (lmp == null) {
            return true;
        }

        // 获取今天的日期（使用统一时区）
        LocalDate today = LocalDate.now(clock);

        // 未来日期交由 @PastOrPresent 验证，避免错误消息冲突
        if (lmp.isAfter(today)) {
            return true;
        }

        // 计算距今天数
        long days = ChronoUnit.DAYS.between(lmp, today);

        // 验证是否在允许范围内
        return days <= maxDays;
    }
}