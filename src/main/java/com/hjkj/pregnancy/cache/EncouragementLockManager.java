package com.hjkj.pregnancy.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 每日鼓励语录并发锁管理器
 * <p>
 * 使用 per-key 锁策略，确保同一用户同一天只会触发一次 AI 调用。
 * Key 格式：openId + "_" + date
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Slf4j
@Component
public class EncouragementLockManager {

    private final Clock clock;
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();

    public EncouragementLockManager(Clock clock) {
        this.clock = clock;
    }

    /**
     * 获取今日锁
     *
     * @param openId 用户唯一标识
     * @return 锁对象
     */
    public Lock getLock(String openId) {
        LocalDate today = LocalDate.now(clock);
        String key = buildKey(openId, today);

        return locks.computeIfAbsent(key, k -> {
            log.debug("创建新锁: key={}", k);
            return new ReentrantLock();
        });
    }

    /**
     * 构建锁 Key
     */
    private String buildKey(String openId, LocalDate date) {
        return openId + "_" + date;
    }
}