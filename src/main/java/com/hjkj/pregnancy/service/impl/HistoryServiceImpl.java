package com.hjkj.pregnancy.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjkj.pregnancy.entity.Recipe;
import com.hjkj.pregnancy.entity.UserHistory;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.enums.FeedbackAction;
import com.hjkj.pregnancy.exception.BusinessException;
import com.hjkj.pregnancy.model.ai.AiMealRecord;
import com.hjkj.pregnancy.model.dto.HistorySearchRequest;
import com.hjkj.pregnancy.model.vo.MealVO;
import com.hjkj.pregnancy.repository.RecipeRepository;
import com.hjkj.pregnancy.repository.UserHistoryRepository;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.repository.specification.UserHistorySpecification;
import com.hjkj.pregnancy.service.HistoryService;
import com.hjkj.pregnancy.entity.UserFeedback;
import com.hjkj.pregnancy.model.PageResult;
import com.hjkj.pregnancy.repository.UserFeedbackRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 浏览历史服务实现类
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final UserHistoryRepository historyRepository;
    private final UserProfileRepository userProfileRepository;
    private final RecipeRepository recipeRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void recordHistory(Long userId, Long recipeId) {
        log.info("记录浏览历史: userId={}, recipeId={}", userId, recipeId);

        UserHistory history = UserHistory.builder()
                .userId(userId)
                .recipeId(recipeId)
                .build();

        historyRepository.save(history);
    }

    @Override
    public List<Long> getRecentRecipeIds(Long userId, int limit) {
        List<Long> recipeIds = historyRepository.findRecentRecipeIds(userId, limit);

        // 如果没有历史记录，返回空列表
        if (recipeIds.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("用户 {} 最近浏览了 {} 个食谱", userId, recipeIds.size());
        return recipeIds;
    }

    @Override
    public List<String> getRecentDishNames(Long userId, int limit) {
        List<Long> recipeIds = historyRepository.findRecentRecipeIds(userId, limit);

        if (recipeIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据ID查询菜品名称
        List<String> dishNames = recipeIds.stream()
                .map(recipeId -> recipeRepository.findById(recipeId).orElse(null))
                .filter(recipe -> recipe != null)
                .map(Recipe::getDishName)
                .collect(Collectors.toList());

        log.info("用户 {} 最近浏览了 {} 个菜品: {}", userId, dishNames.size(), dishNames);
        return dishNames;
    }

    @Override
    public PageResult<MealVO> getUserHistory(String openId, int page, int size) {
        log.info("获取用户浏览历史: openId={}, page={}, size={}", openId, page, size);

        // 查询用户
        UserProfile user = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 分页查询浏览历史
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UserHistory> historyPage = historyRepository.findByUserIdOrderByViewedAtDesc(user.getId(), pageable);

        // 收集食谱ID
        List<Long> recipeIds = historyPage.getContent().stream()
                .map(UserHistory::getRecipeId)
                .collect(Collectors.toList());

        // 批量查询食谱详情
        List<Recipe> recipes = recipeRepository.findAllById(recipeIds);

        // 批量查询用户反馈
        List<UserFeedback> feedbacks = userFeedbackRepository.findByUserIdAndRecipeIdIn(user.getId(), recipeIds);

        // 构建ID到Recipe的映射
        java.util.Map<Long, Recipe> recipeMap = recipes.stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r));

        // 构建ID到Feedback的映射
        java.util.Map<Long, String> feedbackMap = feedbacks.stream()
                .collect(Collectors.toMap(UserFeedback::getRecipeId, uf -> uf.getAction().name(), (v1, v2) -> v1));

        // 转换为VO
        List<MealVO> voList = historyPage.getContent().stream()
                .map(history -> {
                    Recipe recipe = recipeMap.get(history.getRecipeId());
                    if (recipe == null) {
                        return null;
                    }
                    String feedbackAction = feedbackMap.get(history.getRecipeId());
                    return convertToMealVO(recipe, history, feedbackAction);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return PageResult.<MealVO>builder()
                .total(historyPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(historyPage.getTotalPages())
                .list(voList)
                .build();
    }

    @Override
    public MealVO getMealDetail(String openId, Long recipeId) {
        log.info("获取菜单详情: openId={}, recipeId={}", openId, recipeId);

        // 查询用户
        UserProfile user = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new BusinessException(com.hjkj.pregnancy.exception.ErrorCode.USER_NOT_FOUND));

        // 查询该用户的浏览历史记录
        UserHistory history = historyRepository.findByUserIdAndRecipeId(user.getId(), recipeId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(com.hjkj.pregnancy.exception.ErrorCode.MEAL_HISTORY_NOT_FOUND));

        // 查询食谱详情
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(com.hjkj.pregnancy.exception.ErrorCode.MEAL_NOT_FOUND));

        // 查询用户反馈
        String feedbackAction = userFeedbackRepository.findByUserIdAndRecipeId(user.getId(), recipeId)
                .stream()
                .findFirst()
                .map(uf -> uf.getAction().name())
                .orElse(null);

        // 转换为VO
        MealVO mealVO = convertToMealVO(recipe, history, feedbackAction);
        if (mealVO == null) {
            throw new BusinessException(com.hjkj.pregnancy.exception.ErrorCode.MEAL_DATA_ERROR);
        }

        log.info("成功获取菜单详情: dishName={}", mealVO.getDishName());
        return mealVO;
    }

    @Override
    public PageResult<MealVO> searchUserHistory(String openId, HistorySearchRequest request) {
        log.info("搜索用户浏览历史: openId={}, request={}", openId, request);

        // 查询用户
        UserProfile user = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new BusinessException(com.hjkj.pregnancy.exception.ErrorCode.USER_NOT_FOUND));

        // 解析搜索条件
        FeedbackAction feedbackAction = null;
        if (request.getFeedbackAction() != null && !request.getFeedbackAction().isBlank()) {
            try {
                feedbackAction = FeedbackAction.valueOf(request.getFeedbackAction().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(com.hjkj.pregnancy.exception.ErrorCode.INVALID_FEEDBACK_ACTION, 
                                          "无效的反馈动作: " + request.getFeedbackAction());
            }
        }

        // 构建查询规格
        Specification<UserHistory> spec = UserHistorySpecification.buildSpecification(
                user.getId(),
                request.getDishName(),
                feedbackAction,
                request.getMealType()
        );

        // 分页查询（按浏览时间倒序）
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "viewedAt")
        );

        Page<UserHistory> historyPage = historyRepository.findAll(spec, pageable);

        // 收集食谱ID
        List<Long> recipeIds = historyPage.getContent().stream()
                .map(UserHistory::getRecipeId)
                .collect(Collectors.toList());

        if (recipeIds.isEmpty()) {
            log.info("搜索结果为空");
            return PageResult.<MealVO>builder()
                    .total(0L)
                    .page(request.getPage())
                    .size(request.getSize())
                    .totalPages(0)
                    .list(new ArrayList<>())
                    .build();
        }

        // 批量查询食谱详情
        List<Recipe> recipes = recipeRepository.findAllById(recipeIds);

        // 批量查询用户反馈
        List<UserFeedback> feedbacks = userFeedbackRepository.findByUserIdAndRecipeIdIn(user.getId(), recipeIds);

        // 构建ID到Recipe的映射
        java.util.Map<Long, Recipe> recipeMap = recipes.stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r));

        // 构建ID到Feedback的映射
        java.util.Map<Long, String> feedbackMap = feedbacks.stream()
                .collect(Collectors.toMap(UserFeedback::getRecipeId, uf -> uf.getAction().name(), (v1, v2) -> v1));

        // 转换为VO
        List<MealVO> voList = historyPage.getContent().stream()
                .map(history -> {
                    Recipe recipe = recipeMap.get(history.getRecipeId());
                    if (recipe == null) {
                        return null;
                    }
                    String feedback = feedbackMap.get(history.getRecipeId());
                    return convertToMealVO(recipe, history, feedback);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        log.info("搜索完成，共找到 {} 条记录", historyPage.getTotalElements());

        return PageResult.<MealVO>builder()
                .total(historyPage.getTotalElements())
                .page(request.getPage())
                .size(request.getSize())
                .totalPages(historyPage.getTotalPages())
                .list(voList)
                .build();
    }

    /**
     * 将Recipe转换为MealVO (包含历史记录时间)
     */
    private MealVO convertToMealVO(Recipe recipe, UserHistory history, String feedbackAction) {
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
                    .mealType(recipe.getMealType()) // 从Recipe获取
                    .createTime(history != null ? history.getViewedAt().toString() : null) // 从History获取
                    .dishName(aiRecord.dishName())
                    .reason(aiRecord.reason())
                    .tags(aiRecord.tags())
                    .safety(aiRecord.safety())
                    .cookTime(aiRecord.cookTime())
                    .ingredients(aiRecord.ingredients())
                    .steps(aiRecord.steps())
                    .husbandTask(aiRecord.husbandTask())
                    .nutrition(nutrition)
                    .feedbackAction(feedbackAction)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("解析食谱JSON失败: recipeId={}", recipe.getId(), e);
            return null;
        }
    }
}
