package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.model.vo.MealVO;
import com.hjkj.pregnancy.service.HistoryService;
import com.hjkj.pregnancy.service.RecommendationService;
import com.hjkj.pregnancy.utils.Result;
import com.hjkj.pregnancy.validator.ValidMealType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 食谱推荐控制器
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/v1/meal")
@RequiredArgsConstructor
@Tag(name = "食谱推荐", description = "智能食谱推荐相关接口")
public class MealController {

    private final RecommendationService recommendationService;
    private final HistoryService historyService;

    @GetMapping(value = "/recommend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式推荐食谱", description = "使用SSE流式返回AI生成的食谱内容")
    public SseEmitter recommendStream(
            @Parameter(description = "用户唯一标识", required = true)
            @NotBlank(message = "用户标识不能为空")
            @RequestParam String openId,
            
            @Parameter(description = "餐次类型：BREAKFAST/LUNCH/DINNER", required = true)
            @ValidMealType
            @RequestParam String mealType) {
        
        log.info("收到流式推荐请求: openId={}, mealType={}", openId, mealType);
        
        // 参数验证已由 @ValidMealType 注解处理，无需手动验证
        return recommendationService.recommendMealStream(openId, mealType.toUpperCase());
    }

    @GetMapping("/history")
    @Operation(summary = "获取浏览历史", description = "查询用户的浏览历史记录")
    public Result<List<MealVO>> getHistory(
            @Parameter(description = "用户唯一标识", required = true)
            @RequestParam String openId) {
        try {
            log.info("查询浏览历史: openId={}", openId);
            List<MealVO> history = historyService.getUserHistory(openId);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取浏览历史失败", e);
            return Result.error(e.getMessage());
        }
    }
}

