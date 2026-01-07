package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.model.dto.FeedbackRequest;
import com.hjkj.pregnancy.service.FeedbackService;
import com.hjkj.pregnancy.utils.AuthContext;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户反馈接口
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Tag(name = "用户反馈", description = "用户对食谱的反馈（喜欢/不喜欢/吃腻了）")
@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @RequireLogin
    @PostMapping
    @Operation(summary = "提交反馈", description = "对食谱进行反馈，需要登录token")
    public Result<Void> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        // 从token中获取openId
        String openId = AuthContext.getCurrentOpenId();
        log.info("收到用户反馈: openId={}, recipeId={}, action={}", openId, request.getRecipeId(), request.getAction());
        
        feedbackService.submitFeedback(openId, request.getRecipeId(), request.getAction(), request.getReason());
        return Result.success();
    }

}
