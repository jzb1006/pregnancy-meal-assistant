package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.service.FoodSafetyService;
import com.hjkj.pregnancy.utils.AuthContext;
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

    @RequireLogin
    @GetMapping(value = "/check", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "能不能吃查询 (流式)", description = "AI实时评估食物安全性 (SSE流)，需要登录token")
    public Flux<String> checkFood(
            @Parameter(description = "查询词 (如: 螃蟹)", required = true) @RequestParam String query) {
        // 从token中获取openId
        String openId = AuthContext.getCurrentOpenId();
        log.info("收到食物安全查询: openId={}, query={}", openId, query);
        return foodSafetyService.checkFoodSafety(openId, query);
    }

    @RequireLogin
    @GetMapping(value = "/check-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "能不能吃查询(非流式)", description = "返回完整JSON,适配小程序，需要登录token")
    public reactor.core.publisher.Mono<String> checkFoodJson(
            @Parameter(description = "查询词 (如: 螃蟹)", required = true) @RequestParam String query) {
        // 从token中获取openId
        String openId = AuthContext.getCurrentOpenId();
        log.info("收到食物安全查询(非流式): openId={}, query={}", openId, query);
        
        // 聚合所有流式chunks为完整JSON
        return foodSafetyService.checkFoodSafety(openId, query)
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString);
    }

    @RequireLogin
    @GetMapping("/tip")
    @Operation(summary = "每日营养贴士", description = "获取一条随机营养建议，需要登录token")
    public Result<String> getNutritionTip() {
        // 从token中获取openId
        String openId = AuthContext.getCurrentOpenId();
        log.info("获取营养贴士: openId={}", openId);
        return Result.success(foodSafetyService.getNutritionTip(openId));
    }
}
