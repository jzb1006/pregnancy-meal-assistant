package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import com.hjkj.pregnancy.cache.EncouragementCacheManager;
import com.hjkj.pregnancy.cache.EncouragementLockManager;
import com.hjkj.pregnancy.entity.DailyEncouragement;
import com.hjkj.pregnancy.enums.MoodType;
import com.hjkj.pregnancy.enums.PregnancyStage;
import com.hjkj.pregnancy.model.dto.EncouragementResponse;
import com.hjkj.pregnancy.model.vo.DailyEncouragementVO;
import com.hjkj.pregnancy.model.vo.UserStatusVO;
import com.hjkj.pregnancy.repository.DailyEncouragementRepository;
import com.hjkj.pregnancy.service.ChatModelService;
import com.hjkj.pregnancy.service.DailyEncouragementService;
import com.hjkj.pregnancy.service.EncouragementPromptBuilder;
import com.hjkj.pregnancy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;

/**
 * 每日鼓励语录服务实现类
 *
 * @author Zhibin Jiang
 * @since 2025-12-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyEncouragementServiceImpl implements DailyEncouragementService {

    private final UserService userService;
    private final DailyEncouragementRepository dailyEncouragementRepository;
    private final ChatModelService chatModelService;
    private final EncouragementPromptBuilder promptBuilder;
    private final EncouragementCacheManager cacheManager;
    private final EncouragementLockManager lockManager;
    private final Clock clock;


    @Override
    public DailyEncouragementVO getDailyEncouragement(String openId, MoodType mood) {
        log.info("获取每日鼓励语录: openId={}, mood={}", openId, mood);

        // 1. 检查缓存
        var cached = cacheManager.get(openId);
        if (cached.isPresent()) {
            log.debug("从缓存返回: openId={}", openId);
            return cached.get();
        }

        // 2. 使用 per-key 锁防止并发
        Lock lock = lockManager.getLock(openId);
        lock.lock();
        try {
            // 2.1 双重检查（Double-Check）
            cached = cacheManager.get(openId);
            if (cached.isPresent()) {
                log.debug("锁内二次检查命中缓存: openId={}", openId);
                return cached.get();
            }

            LocalDate today = LocalDate.now(clock);

            // 3. 查询数据库
            var existingRecord = dailyEncouragementRepository.findByOpenIdAndRecordDate(openId, today);
            if (existingRecord.isPresent()) {
                log.debug("今日已生成，从数据库返回: openId={}", openId);
                DailyEncouragementVO result = buildFromEntity(existingRecord.get());
                cacheManager.put(openId, result);
                return result;
            }

            // 4. 如果 mood 为 null，说明是查询模式，且无记录，直接返回 null
            if (mood == null) {
                log.info("查询模式且今日未生成，返回空: openId={}", openId);
                return null;
            }

            // 5.未生成且有 mood，调用 AI 生成
            log.info("今日未生成，调用 AI: openId={}, mood={}", openId, mood);
            DailyEncouragementVO result = generateWithAi(openId, mood, today);

            // 6. 存储到数据库和缓存
            saveToDatabase(openId, result, mood, today);
            cacheManager.put(openId, result);

            return result;

        } finally {
            lock.unlock();
        }
    }

    /**
     * 从实体构建 VO
     */
    private DailyEncouragementVO buildFromEntity(DailyEncouragement entity) {
        return DailyEncouragementVO.builder()
                .encouragement(entity.getEncouragementText())
                .week(entity.getWeek())
                .mood(entity.getMood().getLabel())
                .babySize(entity.getBabySize())
                .generatedAt(entity.getGeneratedAt())
                .isFallback(entity.getIsFallback())
                .build();
    }

    /**
     * 调用 AI 生成鼓励语录
     */
    private DailyEncouragementVO generateWithAi(String openId, MoodType mood, LocalDate today) {
        try {
            // 获取用户状态
            UserStatusVO status = userService.getUserStatus(openId);
            PregnancyStage stage = PregnancyStage.fromWeek(status.getWeek());

            // 构建提示词
            String prompt = promptBuilder.buildPrompt(status.getWeek(), mood, stage);

            // 调用 AI
            AiAdvisorContext context = AiAdvisorContext.of(
                    openId,
                    "daily_encouragement",
                    "GENERATE"
            );

            EncouragementResponse response = chatModelService.call(
                    prompt,
                    EncouragementResponse.class,
                    context
            );

            // 验证 AI 返回结果
            validateAiResponse(response);

            return DailyEncouragementVO.builder()
                    .encouragement(response.getEncouragement())
                    .week(status.getWeek())
                    .mood(mood.getLabel())
                    .babySize(response.getBabySize())
                    .generatedAt(LocalDateTime.now(clock))
                    .isFallback(false)
                    .build();

        } catch (Exception e) {
            log.error("AI 生成失败，使用降级文案: openId={}, mood={}", openId, mood, e);
            return generateFallbackEncouragement(openId, mood);
        }
    }

    /**
     * 验证 AI 返回结果
     */
    private void validateAiResponse(EncouragementResponse response) {
        if (response == null || response.getEncouragement() == null || response.getBabySize() == null) {
            throw new IllegalArgumentException("AI 返回结果为空");
        }
        if (response.getEncouragement().length() > 50) {
            log.warn("AI 返回的鼓励语录超过50字，将截断: length={}", response.getEncouragement().length());
        }
        if (!response.getBabySize().startsWith("像个")) {
            log.warn("宝宝描述格式不符合预期: {}", response.getBabySize());
        }
    }

    /**
     * 生成降级文案
     */
    private DailyEncouragementVO generateFallbackEncouragement(String openId, MoodType mood) {
        UserStatusVO status = userService.getUserStatus(openId);
        PregnancyStage stage = PregnancyStage.fromWeek(status.getWeek());

        String encouragement = getFallbackText(stage, mood);
        String babySize = getFallbackBabySize(status.getWeek());

        return DailyEncouragementVO.builder()
                .encouragement(encouragement)
                .week(status.getWeek())
                .mood(mood.getLabel())
                .babySize(babySize)
                .generatedAt(LocalDateTime.now(clock))
                .isFallback(true)
                .build();
    }

    /**
     * 获取降级鼓励文案
     */
    private String getFallbackText(PregnancyStage stage, MoodType mood) {
        return switch (stage) {
            case EARLY -> switch (mood) {
                case TIRED, ANXIOUS -> "妈妈，我知道你辛苦了，我会努力扎根成长的，一起加油哦！❤️";
                default -> "妈妈，我正在你肚子里努力长大，期待和你见面的那一天！😊";
            };
            case MIDDLE -> switch (mood) {
                case TIRED, ANXIOUS -> "妈妈，你的辛苦我都能感受到，我会快快长大报答你的！💕";
                default -> "妈妈，我在你肚子里踢踢腿，是想告诉你我很健康很开心哦！🤗";
            };
            case LATE -> switch (mood) {
                case TIRED, ANXIOUS -> "妈妈，我知道你背痛腿肿，再坚持一下，我们马上就能见面啦！🥰";
                default -> "妈妈，我已经准备好了，很快就能抱抱你啦，好期待！😍";
            };
            case OVERDUE -> "妈妈，我准备好了，我们马上就能见面啦！爱你！❤️";
        };
    }

    /**
     * 获取降级宝宝描述
     */
    private String getFallbackBabySize(int week) {
        if (week <= 8) return "像颗蓝莓";
        if (week <= 12) return "像个柠檬";
        if (week <= 16) return "像个苹果";
        if (week <= 20) return "像根香蕉";
        if (week <= 24) return "像个玉米";
        if (week <= 28) return "像颗椰子";
        if (week <= 32) return "像个菠萝";
        if (week <= 36) return "像个南瓜";
        return "像个西瓜";
    }

    /**
     * 存储到数据库
     */
    @Transactional
    protected void saveToDatabase(String openId, DailyEncouragementVO vo, MoodType mood, LocalDate today) {
        DailyEncouragement entity = DailyEncouragement.builder()
                .openId(openId)
                .mood(mood)
                .encouragementText(vo.getEncouragement())
                .babySize(vo.getBabySize())
                .week(vo.getWeek())
                .generatedAt(vo.getGeneratedAt())
                .recordDate(today)
                .isFallback(vo.getIsFallback())
                .build();

        dailyEncouragementRepository.save(entity);

        log.info("鼓励语录已存储: openId={}, isFallback={}", openId, vo.getIsFallback());
    }
}