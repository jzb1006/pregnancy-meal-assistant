package com.hjkj.pregnancy.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjkj.pregnancy.constants.PregnancyConstants;
import com.hjkj.pregnancy.entity.Recipe;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.exception.AiServiceException;
import com.hjkj.pregnancy.exception.UserNotFoundException;
import com.hjkj.pregnancy.model.ai.AiMealRecord;
import com.hjkj.pregnancy.model.vo.MealVO;
import com.hjkj.pregnancy.repository.RecipeRepository;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.HistoryService;
import com.hjkj.pregnancy.service.PromptBuilder;
import com.hjkj.pregnancy.service.RecommendationService;
import com.hjkj.pregnancy.utils.AgeUtil;
import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import com.hjkj.pregnancy.advisor.AiRequestAdvisor;
import com.hjkj.pregnancy.service.FeedbackService;
import com.hjkj.pregnancy.utils.BmiUtil;
import com.hjkj.pregnancy.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 智能推荐服务实现类
 *
 * @author Zhibin Jiang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserProfileRepository userProfileRepository;
    private final RecipeRepository recipeRepository;
    private final HistoryService historyService;
    private final DashScopeChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final AiRequestAdvisor aiRequestAdvisor;
    private final FeedbackService feedbackService;

    @Qualifier("aiStreamExecutor")
    private final Executor aiStreamExecutor;

    @Value("${pregnancy.history.recent-count:20}")
    private int historyRecentCount;

    @Value("${pregnancy.history.ai-history-count:5}")
    private int aiHistoryCount;

    @Value("${pregnancy.history.enable-history-in-prompt:true}")
    private boolean enableHistoryInPrompt;

    @Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
    private String aiModel;

    @Value("${spring.ai.dashscope.chat.options.temperature:0.7}")
    private Double aiTemperature;

    /**
     * 构建AI提示词（使用PromptBuilder）
     */
    private String buildPrompt(int week, String stage, String bmiCategory, double bmi, String mealType,
            int age, String ageGroupLabel, String ageAdvice, String ageKeywords,
            List<String> recentDishNames, String cuisinePreference,
            String allergies, String dietaryRestrictions, String preferences,
            List<com.hjkj.pregnancy.model.dto.DislikedDishDTO> dislikedDishes) {
        PromptBuilder.PromptContext context = PromptBuilder.PromptContext.builder()
                .week(week)
                .stage(stage)
                .bmiCategory(bmiCategory)
                .bmi(bmi)
                .mealType(mealType)
                .age(age)
                .ageGroupLabel(ageGroupLabel)
                .ageAdvice(ageAdvice)
                .ageKeywords(ageKeywords)
                .recentDishNames(recentDishNames)
                .cuisinePreference(cuisinePreference)
                .allergies(allergies)
                .dietaryRestrictions(dietaryRestrictions)
                .preferences(preferences)
                .dislikedDishes(dislikedDishes)
                .build();

        return PromptBuilder.buildMealRecommendationPrompt(context);
    }

    /**
     * 将AI生成的食谱保存到数据库
     */
    private Recipe saveRecipeToDb(AiMealRecord aiOutput, int week, String bmiCategory, String mealType) {
        try {
            String contentJson = objectMapper.writeValueAsString(aiOutput);
            String tags = String.join(",", aiOutput.tags());

            Recipe recipe = Recipe.builder()
                    .dishName(aiOutput.dishName())
                    .tags(tags)
                    .bmiCategory(bmiCategory)
                    .mealType(mealType)
                    .pregnancyWeek(week)
                    .contentJson(contentJson)
                    .build();

            recipe = recipeRepository.save(recipe);
            log.debug("食谱已保存到数据库: recipeId={}, dishName={}", recipe.getId(), recipe.getDishName());

            return recipe;

        } catch (JsonProcessingException e) {
            log.error("保存食谱失败", e);
            throw new AiServiceException("保存食谱失败", e);
        }
    }

    /**
     * 将Recipe转换为MealVO
     */
    private MealVO convertToMealVO(Recipe recipe) {
        try {
            AiMealRecord aiRecord = objectMapper.readValue(recipe.getContentJson(), AiMealRecord.class);

            MealVO.NutritionInfo nutrition = null;
            if (aiRecord.nutrition() != null) {
                nutrition = MealVO.NutritionInfo.builder()
                        .calories(aiRecord.nutrition().calories())
                        .protein(aiRecord.nutrition().protein())
                        .fat(aiRecord.nutrition().fat())
                        .carbohydrate(aiRecord.nutrition().carbohydrate())
                        .build();
            }

            return MealVO.builder()
                    .id(recipe.getId())
                    .dishName(aiRecord.dishName())
                    .reason(aiRecord.reason())
                    .tags(aiRecord.tags())
                    .safety(aiRecord.safety())
                    .cookTime(aiRecord.cookTime())
                    .ingredients(aiRecord.ingredients())
                    .steps(aiRecord.steps())
                    .husbandTask(aiRecord.husbandTask())
                    .nutrition(nutrition)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("解析食谱JSON失败: recipeId={}", recipe.getId(), e);
            throw new AiServiceException("解析食谱数据失败", e);
        }
    }

    /**
     * 流式推荐食谱
     */
    @Override
    public SseEmitter recommendMealStream(String openId, String mealType) {
        log.debug("开始流式推荐: openId={}, mealType={}", openId, mealType);

        // 创建SSE发射器，超时时间5分钟
        SseEmitter emitter = new SseEmitter(PregnancyConstants.Timeout.SSE_TIMEOUT_MILLIS);

        // 使用自定义线程池异步处理流式响应
        CompletableFuture.runAsync(() -> {
            Long userId = null;
            String bmiCategory = "ALL";
            try {
                // 1. 查人：获取用户档案
                UserProfile user = userProfileRepository.findByOpenId(openId)
                        .orElseThrow(() -> new UserNotFoundException(openId));
                userId = user.getId();

                // 2. 算命：计算当前状态
                int week = DateUtil.calculatePregnancyWeek(user.getLastMenstrualPeriod());
                double bmi = BmiUtil.calculateBmi(user.getHeight(), user.getCurrentWeight());
                bmiCategory = BmiUtil.getBmiCategory(bmi);
                String stage = DateUtil.getPregnancyStage(week);

                // 计算年龄相关信息
                int age = AgeUtil.calculateAge(user.getBirthDate());
                AgeUtil.AgeGroup ageGroup = AgeUtil.getAgeGroup(age);
                String ageGroupLabel = AgeUtil.getAgeGroupLabel(ageGroup);
                String ageAdvice = AgeUtil.getNutritionAdvice(age);
                String ageKeywords = AgeUtil.getDietKeywords(age);

                // 获取饮食偏好
                String cuisinePreference = user.getCuisinePreference() != null
                        ? user.getCuisinePreference().getLabel()
                        : null;

                log.debug("用户状态: week={}, bmi={}, bmiCategory={}, stage={}, age={}, ageGroup={}, cuisinePreference={}",
                        week, bmi, bmiCategory, stage, age, ageGroupLabel, cuisinePreference);

                // 3. 查史：获取最近看过的菜 ID 和名称
                List<Long> viewedIds = historyService.getRecentRecipeIds(user.getId(), historyRecentCount);
                // 根据配置决定是否获取历史菜品名称（用于AI提示词）
                List<String> viewedDishNames = enableHistoryInPrompt
                        ? historyService.getRecentDishNames(user.getId(), aiHistoryCount)
                        : Collections.emptyList();

                // [New] 获取不喜欢的菜 ID 和 详情
                List<Long> dislikedIds = feedbackService.getDislikedRecipeIds(user.getId());
                List<com.hjkj.pregnancy.model.dto.DislikedDishDTO> dislikedDishes = feedbackService
                        .getRecentDislikedDishes(user.getId());

                // 合并排除列表
                List<Long> excludeIds = new java.util.ArrayList<>();
                if (viewedIds != null)
                    excludeIds.addAll(viewedIds);
                if (dislikedIds != null)
                    excludeIds.addAll(dislikedIds);

                if (excludeIds.isEmpty()) {
                    excludeIds = Collections.singletonList(-1L);
                }

                // 4. 决策：先查库，库里没有再调 AI
                Recipe recipe = recipeRepository.findSmartMatch(bmiCategory, mealType, week, excludeIds)
                        .orElse(null);

                if (recipe != null) {
                    // 如果数据库有数据，直接返回完整数据
                    log.debug("从数据库中找到合适的食谱: recipeId={}", recipe.getId());
                    MealVO mealVO = convertToMealVO(recipe);
                    emitter.send(SseEmitter.event()
                            .name("complete")
                            .data(mealVO));

                    // 记录浏览历史
                    historyService.recordHistory(user.getId(), recipe.getId());
                    emitter.complete();

                } else {
                    // 5. 调 AI：构建 Prompt 并使用流式调用
                    log.debug("数据库中没有合适的食谱，调用AI流式生成新食谱");
                    String prompt = buildPrompt(week, stage, bmiCategory, bmi, mealType,
                            age, ageGroupLabel, ageAdvice, ageKeywords, viewedDishNames, cuisinePreference,
                            user.getAllergies(), user.getDietaryRestrictions(), user.getPreferences(),
                            dislikedDishes);

                    // 发送开始事件
                    emitter.send(SseEmitter.event()
                            .name("start")
                            .data("开始生成食谱..."));

                    // 流式调用AI
                    callAIStream(prompt, emitter, userId, week, bmiCategory, mealType, openId);
                }

            } catch (Exception e) {
                log.error("流式推荐失败", e);
                // 尝试降级
                if (userId != null) {
                    handleMealFallback(emitter, userId, bmiCategory, mealType, e.getMessage());
                } else {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(e.getMessage()));
                        emitter.completeWithError(e);
                    } catch (IOException ioException) {
                        log.error("发送错误消息失败", ioException);
                    }
                }
            }
        }, aiStreamExecutor); // 使用自定义线程池

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            log.debug("SSE连接完成");
        });

        return emitter;
    }

    /**
     * 流式调用AI生成食谱（使用 Advisor 拦截）
     */
    private void callAIStream(String prompt, SseEmitter emitter, Long userId,
            int week, String bmiCategory, String mealType, String openId) {
        // 创建 Advisor 上下文
        AiAdvisorContext context = AiAdvisorContext.of(openId, "meal_recommend_stream", mealType);

        try {
            log.debug("开始流式调用AI，Prompt长度: {}, 模型: {}", prompt.length(), aiModel);

            BeanOutputConverter<AiMealRecord> converter = new BeanOutputConverter<>(AiMealRecord.class);
            String format = converter.getFormat();

            String fullPrompt = prompt + "\n\n" + format;

            // 构造 ChatOptions，显式指定模型
            DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                    .model(aiModel)
                    .temperature(aiTemperature)
                    .build();

            Prompt aiPrompt = new Prompt(new UserMessage(fullPrompt), chatOptions);

            // 包装 Advisor 参数
            var advisorParams = AiRequestAdvisor.wrapContext(context);

            // 请求前拦截
            aiPrompt = aiRequestAdvisor.beforeRequest(aiPrompt, advisorParams);

            // 使用stream方法获取流式响应（会使用 Prompt 中的 ChatOptions）
            Flux<ChatResponse> responseFlux = chatModel.stream(aiPrompt);

            // 使用 Advisor 包装流式响应（自动处理拦截和日志）
            Flux<ChatResponse> advisedFlux = aiRequestAdvisor.afterStreamResponse(responseFlux, advisorParams);

            StringBuilder fullResponse = new StringBuilder();

            // 订阅流式响应
            advisedFlux.subscribe(
                    chatResponse -> {
                        // 每次收到一个chunk，发送给客户端
                        String content = chatResponse.getResult().getOutput().getText();
                        fullResponse.append(content);

                        try {
                            emitter.send(SseEmitter.event()
                                    .name("chunk")
                                    .data(content));
                            log.debug("发送chunk: {}", content);
                        } catch (IOException e) {
                            log.error("发送chunk失败", e);
                        }
                    },
                    error -> {
                        // 错误处理（Advisor 已自动记录日志）
                        log.error("AI流式响应错误", error);
                        // 尝试降级
                        handleMealFallback(emitter, userId, bmiCategory, mealType, error.getMessage());
                    },
                    () -> {
                        // 完成处理（Advisor 已自动记录日志）
                        try {
                            log.debug("AI流式响应完成，完整响应长度: {}", fullResponse.length());

                            // 解析完整响应
                            AiMealRecord aiOutput = converter.convert(fullResponse.toString());

                            // 保存到数据库
                            Recipe recipe = saveRecipeToDb(aiOutput, week, bmiCategory, mealType);

                            // 记录浏览历史
                            historyService.recordHistory(userId, recipe.getId());

                            // 转换为VO并发送完成事件
                            MealVO mealVO = convertToMealVO(recipe);
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data(mealVO));

                            emitter.complete();
                            log.debug("流式推荐完成: recipeId={}", recipe.getId());

                        } catch (Exception e) {
                            log.error("处理AI响应失败", e);

                            // 错误拦截（Advisor 会自动记录日志）
                            aiRequestAdvisor.onError(e, advisorParams);

                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("处理AI响应失败: " + e.getMessage()));
                                emitter.completeWithError(e);
                            } catch (IOException ioException) {
                                log.error("发送错误消息失败", ioException);
                            }
                        }
                    });

        } catch (Exception e) {
            log.error("流式调用AI失败", e);

            // 错误拦截（Advisor 会自动记录日志）
            aiRequestAdvisor.onError(e, AiRequestAdvisor.wrapContext(context));

            // 尝试降级
            handleMealFallback(emitter, userId, bmiCategory, mealType, e.getMessage());
        }
    }

    /**
     * 处理降级逻辑：从数据库随机推荐
     */
    private void handleMealFallback(SseEmitter emitter, Long userId, String bmiCategory, String mealType,
            String errorMsg) {
        log.warn("触发AI服务降级: userId={}, bmiCategory={}, mealType={}, error={}", userId, bmiCategory, mealType, errorMsg);

        try {
            // 1. 发送提示消息
            // emitter.send(SseEmitter.event()
            // .name("chunk")
            // .data("\n(AI服务繁忙，正在切换到精选食谱...)\n"));

            // 2. 查库：随机推荐
            Recipe recipe = recipeRepository.findRandomFallback(bmiCategory, mealType)
                    .orElse(null);

            if (recipe != null) {
                log.info("降级成功，推荐本地食谱: recipeId={}", recipe.getId());

                // 3. 记录历史
                historyService.recordHistory(userId, recipe.getId());

                // 4. 返回完整数据
                MealVO mealVO = convertToMealVO(recipe);
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(mealVO));
                emitter.complete();
            } else {
                log.error("降级失败，没有可用的本地食谱");
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("服务暂时繁忙，且无本地缓存，请稍后重试"));
                emitter.completeWithError(new AiServiceException("降级失败"));
            }
        } catch (Exception ex) {
            log.error("处理降级过程中发生错误", ex);
            try {
                emitter.completeWithError(ex);
            } catch (Exception ignored) {
            }
        }
    }
}
