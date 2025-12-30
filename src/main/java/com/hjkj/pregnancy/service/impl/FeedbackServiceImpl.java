package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.entity.UserFeedback;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.enums.FeedbackAction;
import com.hjkj.pregnancy.exception.UserNotFoundException;
import com.hjkj.pregnancy.repository.UserFeedbackRepository;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户反馈服务实现
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final UserFeedbackRepository feedbackRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public void submitFeedback(String openId, Long recipeId, FeedbackAction action, String reason) {
        UserProfile user = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new UserNotFoundException(openId));

        UserFeedback feedback = UserFeedback.builder()
                .userId(user.getId())
                .recipeId(recipeId)
                .action(action)
                .reason(reason)
                .build();

        feedbackRepository.save(feedback);
        log.info("收到用户反馈: userId={}, recipeId={}, action={}", user.getId(), recipeId, action);
    }

    @Override
    public List<Long> getDislikedRecipeIds(Long userId) {
        return feedbackRepository.findRecipeIdsByUserIdAndActionIn(
                userId,
                List.of(FeedbackAction.DISLIKE, FeedbackAction.BORED));
    }

    @Override
    public List<com.hjkj.pregnancy.model.dto.DislikedDishDTO> getRecentDislikedDishes(Long userId) {
        List<FeedbackAction> actions = List.of(FeedbackAction.DISLIKE, FeedbackAction.BORED);
        // 限制只获取最近的 10 条反馈
        return feedbackRepository.findRecentDislikedDishes(userId, actions,
                org.springframework.data.domain.PageRequest.of(0, 10));
    }
}
