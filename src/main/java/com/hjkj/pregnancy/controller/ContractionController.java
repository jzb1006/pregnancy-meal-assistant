package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.entity.ContractionRecord;
import com.hjkj.pregnancy.model.dto.ContractionRequest;
import com.hjkj.pregnancy.service.ContractionService;
import com.hjkj.pregnancy.utils.AuthContext;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/contraction")
@RequiredArgsConstructor
@Tag(name = "宫缩计时", description = "宫缩记录与分析接口")
public class ContractionController {

    private final ContractionService contractionService;

    @RequireLogin
    @PostMapping("/record")
    @Operation(summary = "保存宫缩记录", description = "记录一次宫缩的开始结束时间")
    public Result<ContractionRecord> saveRecord(@Valid @RequestBody ContractionRequest request) {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("保存宫缩记录: openId={}, request={}", openId, request);

            ContractionRecord record = contractionService.saveRecord(openId, request);
            return Result.success(record);
        } catch (Exception e) {
            log.error("保存宫缩记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @GetMapping("/history")
    @Operation(summary = "获取宫缩历史", description = "获取用户的宫缩记录列表")
    public Result<List<ContractionRecord>> getHistory() {
        try {
            String openId = AuthContext.getCurrentOpenId();
            return Result.success(contractionService.getHistory(openId));
        } catch (Exception e) {
            log.error("获取宫缩历史失败", e);
            return Result.error(e.getMessage());
        }
    }
}
