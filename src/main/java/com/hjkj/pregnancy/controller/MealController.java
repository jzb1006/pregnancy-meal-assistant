package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.model.PageResult;
import com.hjkj.pregnancy.model.dto.HistorySearchRequest;
import com.hjkj.pregnancy.model.vo.MealVO;
import com.hjkj.pregnancy.service.HistoryService;
import com.hjkj.pregnancy.service.RecommendationService;
import com.hjkj.pregnancy.utils.Result;
import com.hjkj.pregnancy.validator.ValidMealType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 食谱推荐控制器
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/v1/meal")
@Tag(name = "食谱推荐", description = "智能食谱推荐相关接口")
public class MealController {

    private final RecommendationService recommendationService;
    private final HistoryService historyService;

    public MealController(RecommendationService recommendationService, HistoryService historyService) {
        this.recommendationService = recommendationService;
        this.historyService = historyService;
    }

    @GetMapping(value = "/recommend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式推荐食谱", description = "使用SSE流式返回AI生成的食谱内容")
    public SseEmitter recommendStream(
            @Parameter(description = "用户唯一标识", required = true) @NotBlank(message = "用户标识不能为空") @RequestParam String openId,

            @Parameter(description = "餐次类型:BREAKFAST/LUNCH/DINNER", required = true) @ValidMealType @RequestParam String mealType) {

        log.info("收到流式推荐请求: openId={}, mealType={}", openId, mealType);

        // 参数验证已由 @ValidMealType 注解处理,无需手动验证
        return recommendationService.recommendMealStream(openId, mealType.toUpperCase());
    }

    @GetMapping(value = "/recommend", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "推荐食谱(非流式)", description = "返回完整的食谱JSON,适配小程序")
    public reactor.core.publisher.Mono<String> recommend(
            @Parameter(description = "用户唯一标识", required = true) @NotBlank(message = "用户标识不能为空") @RequestParam String openId,
            @Parameter(description = "餐次类型:BREAKFAST/LUNCH/DINNER", required = true) @ValidMealType @RequestParam String mealType) {

        log.info("收到非流式推荐请求: openId={}, mealType={}", openId, mealType);

        // 调用Service的Flux版本,聚合所有chunks为完整JSON
        return recommendationService.recommendMealFlux(openId, mealType.toUpperCase())
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString);
    }

    @GetMapping("/history/{recipeId}")
    @Operation(summary = "获取菜单详情", description = "根据食谱ID获取菜单的完整详细信息")
    public Result<MealVO> getMealDetail(
            @Parameter(description = "用户唯一标识", required = true) @NotBlank(message = "用户标识不能为空") @RequestParam String openId,
            @Parameter(description = "食谱ID", required = true) @NotNull(message = "食谱ID不能为空") @PathVariable Long recipeId) {
        try {
            log.info("查询菜单详情: openId={}, recipeId={}", openId, recipeId);
            MealVO mealDetail = historyService.getMealDetail(openId, recipeId);
            return Result.success(mealDetail);
        } catch (Exception e) {
            log.error("获取菜单详情失败: recipeId={}", recipeId, e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/history")
    @Operation(summary = "获取浏览历史（支持搜索）", 
               description = "查询用户的浏览历史记录，支持多条件搜索：菜单名模糊搜索、反馈筛选、餐次筛选")
    public Result<PageResult<MealVO>> getHistory(
            @Parameter(description = "用户唯一标识", required = true) @NotBlank(message = "用户标识不能为空") @RequestParam String openId,
            @Parameter(description = "菜单名称（模糊搜索）") @RequestParam(required = false) String dishName,
            @Parameter(description = "反馈动作筛选（LIKE/DISLIKE/BORED）") @RequestParam(required = false) String feedbackAction,
            @Parameter(description = "餐次类型筛选（BREAKFAST/LUNCH/DINNER）") @RequestParam(required = false) String mealType,
            @Parameter(description = "页码(默认1)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小(默认10)", example = "10") @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("查询浏览历史: openId={}, dishName={}, feedbackAction={}, mealType={}, page={}, size={}", 
                     openId, dishName, feedbackAction, mealType, page, size);

            // 构建搜索请求
            HistorySearchRequest request = HistorySearchRequest.builder()
                    .dishName(dishName)
                    .feedbackAction(feedbackAction)
                    .mealType(mealType)
                    .page(page)
                    .size(size)
                    .build();

            // 如果有搜索条件，使用搜索接口；否则使用原有的列表接口
            PageResult<MealVO> history;
            if (request.hasSearchConditions()) {
                history = historyService.searchUserHistory(openId, request);
            } else {
                history = historyService.getUserHistory(openId, page, size);
            }

            return Result.success(history);
        } catch (Exception e) {
            log.error("获取浏览历史失败", e);
            return Result.error(e.getMessage());
        }
    }
}
