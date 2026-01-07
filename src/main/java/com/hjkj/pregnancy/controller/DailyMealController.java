package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.service.DailyRecommendationService;
import com.hjkj.pregnancy.utils.AuthContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/v1/daily-meal")
@RequiredArgsConstructor
@Tag(name = "每日推荐", description = "每日精选食谱相关接口")
public class DailyMealController {

    private final DailyRecommendationService dailyRecommendationService;

    @RequireLogin
    @GetMapping(value = "/recommend", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "获取今日推荐", description = "获取今日的AI精选食谱，需要登录token")
    public reactor.core.publisher.Mono<String> getDailyRecommendation() {
        // 从token中获取openId
        String openId = AuthContext.getCurrentOpenId();
        log.info("获取今日推荐: openId={}", openId);
        
        // Collect all chunks and return as single JSON
        return dailyRecommendationService.getDailyRecommendation(openId)
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString);
    }

    @RequireLogin
    @PostMapping(value = "/swap", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "换一换", description = "不满意当前推荐，重新生成，需要登录token")
    public reactor.core.publisher.Mono<String> swapRecommendation() {
        // 从token中获取openId
        String openId = AuthContext.getCurrentOpenId();
        log.info("换一换推荐: openId={}", openId);
        
        return dailyRecommendationService.swapRecommendation(openId)
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString);
    }
}
