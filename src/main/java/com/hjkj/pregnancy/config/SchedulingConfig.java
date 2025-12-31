package com.hjkj.pregnancy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * <p>
 * 启用 Spring 的定时任务支持，用于缓存清理等定时操作。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}