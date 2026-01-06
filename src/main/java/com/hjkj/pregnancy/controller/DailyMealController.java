package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.service.DailyRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@Validated
@RestController
@RequestMapping("/v1/daily-meal")
@RequiredArgsConstructor
@Tag(name = "每日推荐", description = "每日精选食谱相关接口")
public class DailyMealController {

    private final DailyRecommendationService dailyRecommendationService;

    @GetMapping(value = "/recommend", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "获取今日推荐", description = "获取今日的AI精选食谱")
    public reactor.core.publisher.Mono<String> getDailyRecommendation(
            @Parameter(description = "用户ID", required = true) @RequestParam String openId) {
        // Collect all chunks and return as single JSON
        return dailyRecommendationService.getDailyRecommendation(openId)
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString);
    }

    @PostMapping(value = "/swap", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "换一换", description = "不满意当前推荐，重新生成")
    public reactor.core.publisher.Mono<String> swapRecommendation(
            @Parameter(description = "用户ID", required = true) @RequestParam String openId) {
        return dailyRecommendationService.swapRecommendation(openId)
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString);
    }
}
