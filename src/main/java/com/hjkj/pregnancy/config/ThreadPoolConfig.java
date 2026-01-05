package com.hjkj.pregnancy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * <p>
 * 为异步任务和CompletableFuture提供自定义线程池，避免使用默认的ForkJoinPool
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * AI流式推荐专用线程池
     * <p>
     * 用于处理SSE流式响应的异步任务
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean(name = "aiStreamExecutor")
    public ThreadPoolTaskExecutor aiStreamExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：根据CPU核心数设置
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);

        // 最大线程数：核心线程数的2倍
        executor.setMaxPoolSize(corePoolSize * 2);

        // 队列容量：100个任务
        executor.setQueueCapacity(100);

        // 线程名称前缀
        executor.setThreadNamePrefix("ai-stream-");

        // 线程空闲时间：60秒
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间：30秒
        executor.setAwaitTerminationSeconds(30);

        // 初始化
        executor.initialize();

        log.info("AI流式推荐线程池初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}",
                corePoolSize, corePoolSize * 2, 100);

        return executor;
    }

    /**
     * 通用异步任务线程池
     * <p>
     * 用于处理一般的异步任务，如日志保存、数据统计等
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean(name = "asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(4);

        // 最大线程数
        executor.setMaxPoolSize(8);

        // 队列容量
        executor.setQueueCapacity(200);

        // 线程名称前缀
        executor.setThreadNamePrefix("async-");

        // 线程空闲时间
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(30);

        // 初始化
        executor.initialize();

        log.info("通用异步任务线程池初始化完成 - 核心线程数: 4, 最大线程数: 8, 队列容量: 200");

        return executor;
    }
}

