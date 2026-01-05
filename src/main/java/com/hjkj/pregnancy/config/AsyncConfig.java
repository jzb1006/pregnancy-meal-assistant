package com.hjkj.pregnancy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置
 * 用于支持流式推荐等异步操作
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 配置异步任务线程池
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("初始化异步任务线程池...");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数
        executor.setMaxPoolSize(10);
        
        // 队列容量
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("async-meal-");
        
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("异步任务线程池初始化完成: corePoolSize={}, maxPoolSize={}", 
            executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
}




