package com.hjkj.pregnancy.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjkj.pregnancy.entity.DailyRecommendation;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.exception.AiServiceException;
import com.hjkj.pregnancy.exception.UserNotFoundException;
import com.hjkj.pregnancy.model.ai.AiDailyRecRecord;
import com.hjkj.pregnancy.model.vo.MealVO;
import com.hjkj.pregnancy.repository.DailyRecommendationRepository;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.DailyRecommendationService;
import com.hjkj.pregnancy.service.PromptBuilder;
import com.hjkj.pregnancy.utils.BmiUtil;
import com.hjkj.pregnancy.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyRecommendationServiceImpl implements DailyRecommendationService {

    private final DailyRecommendationRepository dailyRecRepository;
    private final UserProfileRepository userProfileRepository;
    private final DashScopeChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<String> getDailyRecommendation(String openId) {
        UserProfile user = getUser(openId);
        LocalDate today = LocalDate.now();

        // 1. Check Cache
        DailyRecommendation rec = dailyRecRepository.findByUserIdAndRecDate(user.getId(), today)
                .orElse(null);

        if (rec != null) {
            log.info("命中今日推荐缓存: userId={}, date={}", user.getId(), today);
            return Flux.just(serializeMealVO(convertToMealVO(rec)));
        }

        // 2. Generate Stream
        log.info("生成今日推荐(流式): userId={}, date={}", user.getId(), today);
        return generateAndSaveStream(user, today, new ArrayList<>());
    }

    @Override
    public Flux<String> swapRecommendation(String openId) {
        UserProfile user = getUser(openId);
        LocalDate today = LocalDate.now();

        // Check Existing for Rejection History
        DailyRecommendation rec = dailyRecRepository.findByUserIdAndRecDate(user.getId(), today)
                .orElse(null);

        List<String> rejectedHistory = new ArrayList<>();
        if (rec != null) {
            if (StringUtils.hasText(rec.getRejectedHistory())) {
                List<String> existingRejections = Arrays.asList(rec.getRejectedHistory().split(","));
                if (existingRejections.size() >= 5) {
                    return Flux.error(new AiServiceException("今日换一换次数已达上限 (5次)，请明天再试"));
                }
                rejectedHistory.addAll(existingRejections);
            }
            rejectedHistory.add(rec.getDishName());
            log.info("换一换触发(流式): userId={}, 拒绝列表={}", user.getId(), rejectedHistory);
        }

        return generateAndSaveStream(user, today, rejectedHistory);
    }

    private Flux<String> generateAndSaveStream(UserProfile user, LocalDate date, List<String> rejectedList) {
        // 1. Prepare Context & Prompt
        int week = DateUtil.calculatePregnancyWeek(user.getLastMenstrualPeriod());
        double bmi = BmiUtil.calculateBmi(user.getHeight(), user.getCurrentWeight());
        String bmiCategory = BmiUtil.getBmiCategory(bmi);
        String dateStr = date.toString();
        String exclusions = String.join(",", rejectedList);

        PromptBuilder.DailyRecContext context = PromptBuilder.DailyRecContext.builder()
                .dateStr(dateStr)
                .bmi(bmi)
                .bmiCategory(bmiCategory)
                .exclusions(exclusions)
                .build();

        String promptText = PromptBuilder.buildDailyRecPrompt(context);
        BeanOutputConverter<AiDailyRecRecord> converter = new BeanOutputConverter<>(AiDailyRecRecord.class);
        String fullPrompt = promptText + "\n\n" + converter.getFormat();

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .model("qwen-plus")
                .temperature(1.1)
                .build();

        Prompt prompt = new Prompt(new UserMessage(fullPrompt), chatOptions);

        // 2. Stream & Accumulate
        StringBuilder contentBuilder = new StringBuilder();

        return chatModel.stream(prompt)
                .map(chatResponse -> {
                    String content = chatResponse.getResult().getOutput().getText();
                    if (content != null) {
                        contentBuilder.append(content);
                        return content;
                    }
                    return "";
                })
                .doOnComplete(() -> {
                    // 3. Save to DB on Complete
                    try {
                        String fullContent = contentBuilder.toString();
                        log.info("AI流式生成完成，完整内容长度: {}", fullContent.length());

                        // Clean markdown code blocks if present
                        if (fullContent.contains("```json")) {
                            fullContent = fullContent.replaceAll("```json", "").replaceAll("```", "");
                        } else if (fullContent.contains("```")) {
                            fullContent = fullContent.replaceAll("```", "");
                        }

                        AiDailyRecRecord aiRecord = converter.convert(fullContent);

                        DailyRecommendation rec = dailyRecRepository.findByUserIdAndRecDate(user.getId(), date)
                                .orElse(DailyRecommendation.builder()
                                        .userId(user.getId())
                                        .recDate(date)
                                        .weekNum(week)
                                        .createdAt(LocalDateTime.now())
                                        .build());

                        rec.setDishName(aiRecord.dishName());
                        rec.setContentJson(objectMapper.writeValueAsString(aiRecord));
                        rec.setRejectedHistory(String.join(",", rejectedList));

                        dailyRecRepository.save(rec);
                        log.info("推荐数据已保存至数据库: userId={}", user.getId());

                    } catch (Exception e) {
                        log.error("保存推荐数据失败 (流式后处理)", e);
                        // Note: We cannot throw exception to the client here as the stream has finished
                        // or is finishing.
                        // We rely on logs. The client has received the content.
                    }
                })
                .doOnError(e -> log.error("流式生成过程中发生错误", e));
    }

    private MealVO convertToMealVO(DailyRecommendation rec) {
        try {
            AiDailyRecRecord aiRecord = objectMapper.readValue(rec.getContentJson(), AiDailyRecRecord.class);
            return MealVO.builder()
                    .id(rec.getId())
                    .dishName(aiRecord.dishName())
                    .reason(aiRecord.seasonalReason())
                    .tags(aiRecord.nutritionTags())
                    .ingredients(aiRecord.ingredients())
                    .createTime(rec.getRecDate().toString())
                    .safety("GREEN")
                    .cookTime(aiRecord.cookTime())
                    .steps(aiRecord.steps())
                    .husbandTask(aiRecord.husbandTask())
                    .nutrition(convertNutrition(aiRecord.nutrition()))
                    .build();
        } catch (JsonProcessingException e) {
            throw new AiServiceException("解析推荐数据失败", e);
        }
    }

    private String serializeMealVO(MealVO vo) {
        try {
            return objectMapper.writeValueAsString(vo);
        } catch (JsonProcessingException e) {
            log.error("序列化MealVO失败", e);
            return "{}";
        }
    }

    private MealVO.NutritionInfo convertNutrition(AiDailyRecRecord.Nutrition aiNutrition) {
        if (aiNutrition == null) {
            return null;
        }
        return MealVO.NutritionInfo.builder()
                .calories(aiNutrition.calories())
                .protein(aiNutrition.protein())
                .fat(aiNutrition.fat())
                .carbohydrate(aiNutrition.carbohydrate())
                .build();
    }

    private UserProfile getUser(String openId) {
        return userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new UserNotFoundException("用户不存在"));
    }
}
