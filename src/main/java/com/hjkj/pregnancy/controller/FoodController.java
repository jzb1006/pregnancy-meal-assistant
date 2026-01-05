package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.service.FoodSafetyService;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@Validated
@RestController
@RequestMapping("/v1/food")
@RequiredArgsConstructor
@Tag(name = "食品查询", description = "能不能吃与营养知识查询")
public class FoodController {

    private final FoodSafetyService foodSafetyService;

    @GetMapping(value = "/check", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "能不能吃查询 (流式)", description = "AI实时评估食物安全性 (SSE流)")
    public Flux<String> checkFood(
            @Parameter(description = "用户ID (用于获取孕周)", required = true) @RequestParam String openId,
            @Parameter(description = "查询词 (如: 螃蟹)", required = true) @RequestParam String query) {
        return foodSafetyService.checkFoodSafety(openId, query);
    }

    @GetMapping("/tip")
    @Operation(summary = "每日营养贴士", description = "获取一条随机营养建议")
    public Result<String> getNutritionTip(
            @Parameter(description = "用户ID", required = true) @RequestParam String openId) {
        return Result.success(foodSafetyService.getNutritionTip(openId));
    }
}
