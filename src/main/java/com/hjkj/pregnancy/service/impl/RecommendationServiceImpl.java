package com.hjkj.pregnancy.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjkj.pregnancy.entity.Recipe;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.model.ai.AiMealRecord;
import com.hjkj.pregnancy.model.vo.MealVO;
import com.hjkj.pregnancy.repository.RecipeRepository;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.HistoryService;
import com.hjkj.pregnancy.service.RecommendationService;
import com.hjkj.pregnancy.utils.AgeUtil;
import com.hjkj.pregnancy.advisor.AiAdvisorContext;
import com.hjkj.pregnancy.advisor.AiRequestAdvisor;
import com.hjkj.pregnancy.service.AiLogService;
import com.hjkj.pregnancy.utils.BmiUtil;
import com.hjkj.pregnancy.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private final AiLogService aiLogService;

    @Value("${pregnancy.history.recent-count:20}")
    private int historyRecentCount;

    @Value("${pregnancy.history.ai-history-count:5}")
    private int aiHistoryCount;

    @Value("${pregnancy.history.enable-history-in-prompt:true}")
    private boolean enableHistoryInPrompt;

    @Override
    @Transactional
    public MealVO recommendMeal(String openId, String mealType) {
        log.debug("开始智能推荐: openId={}, mealType={}", openId, mealType);

        // 1. 查人：获取用户档案
        UserProfile user = userProfileRepository.findByOpenId(openId)
            .orElseThrow(() -> new RuntimeException("用户不存在，请先完善个人信息"));

        // 2. 算命：计算当前状态
        int week = DateUtil.calculatePregnancyWeek(user.getLastMenstrualPeriod());
        double bmi = BmiUtil.calculateBmi(user.getHeight(), user.getCurrentWeight());
        String bmiCategory = BmiUtil.getBmiCategory(bmi);
        String stage = DateUtil.getPregnancyStage(week);
        
        // 计算年龄相关信息
        int age = AgeUtil.calculateAge(user.getBirthDate());
        AgeUtil.AgeGroup ageGroup = AgeUtil.getAgeGroup(age);
        String ageGroupLabel = AgeUtil.getAgeGroupLabel(ageGroup);
        String ageAdvice = AgeUtil.getNutritionAdvice(age);
        String ageKeywords = AgeUtil.getDietKeywords(age);

        log.debug("用户状态: week={}, bmi={}, bmiCategory={}, stage={}, age={}, ageGroup={}", 
            week, bmi, bmiCategory, stage, age, ageGroupLabel);

        // 3. 查史：获取最近看过的菜 ID 和名称
        List<Long> viewedIds = historyService.getRecentRecipeIds(user.getId(), historyRecentCount);
        // 根据配置决定是否获取历史菜品名称（用于AI提示词）
        List<String> viewedDishNames = enableHistoryInPrompt 
            ? historyService.getRecentDishNames(user.getId(), aiHistoryCount)
            : Collections.emptyList();
        if (viewedIds.isEmpty()) {
            viewedIds = Collections.singletonList(-1L); // 避免SQL语法错误
        }

        // 4. 决策：先查库，库里没有再调 AI
        Recipe recipe = recipeRepository.findSmartMatch(bmiCategory, mealType, week, viewedIds)
            .orElse(null);

        if (recipe == null) {
            log.debug("数据库中没有合适的食谱，调用AI生成新食谱");
            // 5. 调 AI：构建 Prompt
            String prompt = buildPrompt(week, stage, bmiCategory, bmi, mealType, age, ageGroupLabel, ageAdvice, ageKeywords, viewedDishNames);
            AiMealRecord aiOutput = callAI(prompt, openId, mealType);

            // 6. 入库：保存新生成的菜
            recipe = saveRecipeToDb(aiOutput, week, bmiCategory, mealType);
        } else {
            log.debug("从数据库中找到合适的食谱: recipeId={}", recipe.getId());
        }

        // 7. 留痕：记录本次浏览
        historyService.recordHistory(user.getId(), recipe.getId());

        // 8. 转换为VO返回
        return convertToMealVO(recipe);
    }

    /**
     * 提取菜品关键词（主食材和烹饪方式）
     * 例如："清蒸三文鱼配西兰花" -> "三文鱼、清蒸"
     */
    private String extractKeywords(List<String> dishNames) {
        if (dishNames == null || dishNames.isEmpty()) {
            return "";
        }
        
        // 常见烹饪方式
        String[] cookingMethods = {"清蒸", "红烧", "香煎", "爆炒", "炖", "煮", "烤", "煎", "炒", "焖"};
        
        // 常见食材（用于识别主食材）
        String[] mainIngredients = {
            "三文鱼", "鸡胸肉", "牛肉", "猪肉", "虾", "鱼", "豆腐", "鸡蛋",
            "鸡", "羊肉", "鳕鱼", "排骨", "鸭肉"
        };
        
        StringBuilder keywords = new StringBuilder();
        for (String dishName : dishNames) {
            // 提取烹饪方式
            for (String method : cookingMethods) {
                if (dishName.contains(method)) {
                    keywords.append(method).append("、");
                    break;
                }
            }
            
            // 提取主食材
            for (String ingredient : mainIngredients) {
                if (dishName.contains(ingredient)) {
                    keywords.append(ingredient).append("、");
                    break;
                }
            }
        }
        
        // 去除最后的顿号
        String result = keywords.toString();
        if (result.endsWith("、")) {
            result = result.substring(0, result.length() - 1);
        }
        
        // 如果提取失败，直接返回前3个菜名的简化版
        if (result.isEmpty()) {
            return dishNames.stream()
                .limit(3)
                .collect(Collectors.joining("、"));
        }
        
        return result;
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(int week, String stage, String bmiCategory, double bmi, String mealType, 
                               int age, String ageGroupLabel, String ageAdvice, String ageKeywords, 
                               List<String> recentDishNames) {
        String mealTypeCn = switch (mealType) {
            case "BREAKFAST" -> "早餐";
            case "LUNCH" -> "午餐";
            case "DINNER" -> "晚餐";
            default -> "餐食";
        };

        String bmiAdvice = BmiUtil.getDietAdvice(bmi);

        // 构建历史菜品信息 - 使用简洁格式减少token消耗
        String historyInfo = "";
        if (recentDishNames != null && !recentDishNames.isEmpty()) {
            // 提取关键词：主食材和烹饪方式
            String avoidKeywords = extractKeywords(recentDishNames);
            historyInfo = String.format("""
            
            **避免重复：** 用户最近看过：%s
            **多样性要求：** 主食材、烹饪方式、配菜需完全不同，尝试新风味。
            """, avoidKeywords);
        }

        return String.format("""
            你是一位专业的孕期营养师，请为孕妇推荐一道%s菜谱。
            
            用户信息：
            - 当前孕周：第%d周
            - 孕期阶段：%s
            - BMI分类：%s (%.1f)
            - BMI饮食建议：%s
            - 当前年龄：%d岁
            - 年龄分组：%s
            - 年龄营养建议：%s
            - 年龄饮食关键词：%s%s
            
            要求：
            1. 菜品要符合孕妇营养需求，食材新鲜易得
            2. 烹饪时间控制在30分钟以内
            3. 必须标注食材安全等级（GREEN/YELLOW/RED）
            4. 提供详细的食材用量和烹饪步骤
            5. 包含准确的营养成分信息
            6. 给准爸爸安排一个帮忙的小任务
            7. 根据BMI调整菜品热量和营养配比
            8. **重要：根据年龄分组和营养建议优化菜品，年龄是核心考虑因素**
               - 低龄孕妇：增加高钙、高蛋白、高铁食材
               - 适龄孕妇：营养均衡，品类丰富
               - 高龄孕妇：低GI、高纤维、控制热量
               - 超高龄孕妇：低盐、低糖、易消化、抗氧化
            9. **创新性：每次推荐都要有创意，尝试不同的食材组合、烹饪技法和风味搭配**
            10. **多样性：优先推荐用户从未见过的菜品类型，给用户新鲜感**
            
            请以JSON格式返回，包含以下字段：
            {
              "dish_name": "菜品名称",
              "reason": "推荐理由（100字以内，需体现年龄因素）",
              "tags": ["标签1", "标签2"],
              "safety": "GREEN",
              "cook_time": "15分钟",
              "ingredients": ["食材1 用量", "食材2 用量"],
              "steps": ["步骤1", "步骤2"],
              "husband_task": "准爸爸的任务",
              "nutrition": {
                "calories": 350,
                "protein": 25.0,
                "fat": 12.0,
                "carbohydrate": 30.0
              }
            }
            """, mealTypeCn, week, stage, bmiCategory, bmi, bmiAdvice, age, ageGroupLabel, ageAdvice, ageKeywords, historyInfo);
    }

    /**
     * 调用AI生成食谱
     */
    private AiMealRecord callAI(String prompt) {
        return callAI(prompt, "unknown", "UNKNOWN");
    }

    /**
     * 调用AI生成食谱（使用 Advisor 拦截）
     */
    private AiMealRecord callAI(String prompt, String openId, String mealType) {
        // 创建 Advisor 上下文
        AiAdvisorContext context = AiAdvisorContext.of(openId, "meal_recommend", mealType);

        try {
            log.debug("调用AI生成食谱，Prompt长度: {}", prompt.length());

            BeanOutputConverter<AiMealRecord> converter = new BeanOutputConverter<>(AiMealRecord.class);
            String format = converter.getFormat();

            String fullPrompt = prompt + "\n\n" + format;
            Prompt aiPrompt = new Prompt(new UserMessage(fullPrompt));

            // 包装 Advisor 参数
            var advisorParams = AiRequestAdvisor.wrapContext(context);

            // 请求前拦截
            aiPrompt = aiRequestAdvisor.beforeRequest(aiPrompt, advisorParams);

            // 调用AI
            ChatResponse chatResponse = chatModel.call(aiPrompt);
            
            // 响应后拦截
            chatResponse = aiRequestAdvisor.afterResponse(chatResponse, advisorParams);

            String response = chatResponse.getResult().getOutput().getText();
            log.debug("AI返回结果: {}", response);

            return converter.convert(response);

        } catch (Exception e) {
            log.error("调用AI失败", e);
            
            // 错误拦截（已在 Advisor 中异步保存日志）
            aiRequestAdvisor.onError(e, AiRequestAdvisor.wrapContext(context));
            
            throw new RuntimeException("AI服务暂时不可用，请稍后重试", e);
        }
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
            throw new RuntimeException("保存食谱失败", e);
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
            throw new RuntimeException("解析食谱数据失败", e);
        }
    }

    /**
     * 流式推荐食谱
     */
    @Override
    public SseEmitter recommendMealStream(String openId, String mealType) {
        log.debug("开始流式推荐: openId={}, mealType={}", openId, mealType);

        // 创建SSE发射器，超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        // 异步处理流式响应
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 查人：获取用户档案
                UserProfile user = userProfileRepository.findByOpenId(openId)
                    .orElseThrow(() -> new RuntimeException("用户不存在，请先完善个人信息"));

                // 2. 算命：计算当前状态
                int week = DateUtil.calculatePregnancyWeek(user.getLastMenstrualPeriod());
                double bmi = BmiUtil.calculateBmi(user.getHeight(), user.getCurrentWeight());
                String bmiCategory = BmiUtil.getBmiCategory(bmi);
                String stage = DateUtil.getPregnancyStage(week);
                
                // 计算年龄相关信息
                int age = AgeUtil.calculateAge(user.getBirthDate());
                AgeUtil.AgeGroup ageGroup = AgeUtil.getAgeGroup(age);
                String ageGroupLabel = AgeUtil.getAgeGroupLabel(ageGroup);
                String ageAdvice = AgeUtil.getNutritionAdvice(age);
                String ageKeywords = AgeUtil.getDietKeywords(age);

                log.debug("用户状态: week={}, bmi={}, bmiCategory={}, stage={}, age={}, ageGroup={}", 
                    week, bmi, bmiCategory, stage, age, ageGroupLabel);

                // 3. 查史：获取最近看过的菜 ID 和名称
                List<Long> viewedIds = historyService.getRecentRecipeIds(user.getId(), historyRecentCount);
                // 根据配置决定是否获取历史菜品名称（用于AI提示词）
                List<String> viewedDishNames = enableHistoryInPrompt 
                    ? historyService.getRecentDishNames(user.getId(), aiHistoryCount)
                    : Collections.emptyList();
                if (viewedIds.isEmpty()) {
                    viewedIds = Collections.singletonList(-1L);
                }

                // 4. 决策：先查库，库里没有再调 AI
                Recipe recipe = recipeRepository.findSmartMatch(bmiCategory, mealType, week, viewedIds)
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
                        age, ageGroupLabel, ageAdvice, ageKeywords, viewedDishNames);
                    
                    // 发送开始事件
                    emitter.send(SseEmitter.event()
                        .name("start")
                        .data("开始生成食谱..."));

                    // 流式调用AI
                    callAIStream(prompt, emitter, user.getId(), week, bmiCategory, mealType, openId);
                }

            } catch (Exception e) {
                log.error("流式推荐失败", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ioException) {
                    log.error("发送错误消息失败", ioException);
                }
            }
        });

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
            log.debug("开始流式调用AI，Prompt长度: {}", prompt.length());

            BeanOutputConverter<AiMealRecord> converter = new BeanOutputConverter<>(AiMealRecord.class);
            String format = converter.getFormat();

            String fullPrompt = prompt + "\n\n" + format;
            Prompt aiPrompt = new Prompt(new UserMessage(fullPrompt));

            // 包装 Advisor 参数
            var advisorParams = AiRequestAdvisor.wrapContext(context);

            // 请求前拦截
            aiPrompt = aiRequestAdvisor.beforeRequest(aiPrompt, advisorParams);

            // 使用stream方法获取流式响应
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
                    
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI服务错误: " + error.getMessage()));
                        emitter.completeWithError(error);
                    } catch (IOException e) {
                        log.error("发送错误消息失败", e);
                    }
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
                }
            );

        } catch (Exception e) {
            log.error("流式调用AI失败", e);
            
            // 错误拦截（Advisor 会自动记录日志）
            aiRequestAdvisor.onError(e, AiRequestAdvisor.wrapContext(context));
            
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("AI服务暂时不可用: " + e.getMessage()));
                emitter.completeWithError(e);
            } catch (IOException ioException) {
                log.error("发送错误消息失败", ioException);
            }
        }
    }
}

