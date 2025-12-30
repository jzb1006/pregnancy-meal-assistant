package com.hjkj.pregnancy.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjkj.pregnancy.entity.Recipe;
import com.hjkj.pregnancy.entity.UserHistory;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.model.ai.AiMealRecord;
import com.hjkj.pregnancy.model.vo.MealVO;
import com.hjkj.pregnancy.repository.RecipeRepository;
import com.hjkj.pregnancy.repository.UserHistoryRepository;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.HistoryService;
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
    public List<MealVO> getUserHistory(String openId) {
        log.info("获取用户浏览历史: openId={}", openId);
        
        // 查询用户
        UserProfile user = userProfileRepository.findByOpenId(openId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 查询浏览历史
        List<UserHistory> histories = historyRepository.findByUserIdOrderByViewedAtDesc(user.getId());
        
        // 转换为VO
        return histories.stream()
            .map(history -> {
                Recipe recipe = recipeRepository.findById(history.getRecipeId())
                    .orElse(null);
                if (recipe == null) {
                    return null;
                }
                return convertToMealVO(recipe);
            })
            .filter(vo -> vo != null)
            .collect(Collectors.toList());
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
            return null;
        }
    }
}

