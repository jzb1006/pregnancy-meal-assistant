package com.hjkj.pregnancy.cache;

import com.hjkj.pregnancy.model.vo.DailyEncouragementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每日鼓励语录缓存管理器
 * <p>
 * 使用本地内存缓存（ConcurrentHashMap）加速查询，减少数据库访问。
 * 缓存 Key 格式：openId + "_" + date
 * 每日 00:00 自动清理过期缓存。
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EncouragementCacheManager {

    private final Clock clock;

    /**
     * 缓存容器
     * Key: openId_date (例如: "user123_2025-12-31")
     * Value: CacheEntry (包含数据和日期)
     */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 获取今日缓存
     *
     * @param openId 用户唯一标识
     * @return 今日鼓励语录，如果不存在返回 Optional.empty()
     */
    public Optional<DailyEncouragementVO> get(String openId) {
        LocalDate today = LocalDate.now(clock);
        String key = buildKey(openId, today);

        CacheEntry entry = cache.get(key);
        if (entry != null && entry.date.equals(today)) {
            log.debug("缓存命中: openId={}, date={}", openId, today);
            return Optional.of(entry.data);
        }

        log.debug("缓存未命中: openId={}, date={}", openId, today);
        return Optional.empty();
    }

    /**
     * 存储今日缓存
     *
     * @param openId 用户唯一标识
     * @param data   鼓励语录数据
     */
    public void put(String openId, DailyEncouragementVO data) {
        LocalDate today = LocalDate.now(clock);
        String key = buildKey(openId, today);

        cache.put(key, new CacheEntry(today, data));
        log.debug("缓存已更新: openId={}, date={}", openId, today);
    }

    /**
     * 每日 00:00 清理过期缓存
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanExpiredCache() {
        LocalDate today = LocalDate.now(clock);
        int beforeSize = cache.size();

        cache.entrySet().removeIf(entry -> entry.getValue().date.isBefore(today));

        int afterSize = cache.size();
        log.info("过期缓存清理完成: 清理前={}, 清理后={}, 清理数量={}", beforeSize, afterSize, beforeSize - afterSize);
    }

    /**
     * 构建缓存 Key
     */
    private String buildKey(String openId, LocalDate date) {
        return openId + "_" + date;
    }

    /**
     * 缓存条目（内部类）
     */
    private record CacheEntry(LocalDate date, DailyEncouragementVO data) {
    }
}