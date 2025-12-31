package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.model.dto.FeedbackRequest;
import com.hjkj.pregnancy.service.FeedbackService;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户反馈接口
 * 
 * @author Zhibin Jiang
 */
@Tag(name = "用户反馈接口")
@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "提交反馈")
    @PostMapping
    public Result<Void> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        feedbackService.submitFeedback(request.getOpenId(), request.getRecipeId(), request.getAction(),
                request.getReason());
        return Result.success();
    }

}
