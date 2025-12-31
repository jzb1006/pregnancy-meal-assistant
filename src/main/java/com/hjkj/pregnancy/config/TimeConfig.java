package com.hjkj.pregnancy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * 时间配置类
 * <p>
 * 提供全局统一的 Clock Bean，确保所有时间相关计算使用一致的时区。
 * 使用 Asia/Shanghai 时区作为标准时区，便于测试时通过 @Primary 注解覆盖。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Configuration
public class TimeConfig {

    /**
     * 提供全局 Clock Bean
     * <p>
     * 使用 Asia/Shanghai 时区，确保所有日期时间计算的一致性。
     * 测试时可通过 @Primary 注解提供 Clock.fixed(...) 覆盖此配置。
     * </p>
     *
     * @return 配置为 Asia/Shanghai 时区的 Clock 实例
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Shanghai"));
    }
}