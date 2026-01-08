package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.entity.FetalMovementRecord;
import com.hjkj.pregnancy.model.dto.FetalMovementRequest;
import com.hjkj.pregnancy.service.FetalMovementService;
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
@RequestMapping("/v1/fetal-movement")
@RequiredArgsConstructor
@Tag(name = "数胎动", description = "胎动记录相关接口")
public class FetalMovementController {

    private final FetalMovementService fetalMovementService;

    @RequireLogin
    @PostMapping("/record")
    @Operation(summary = "保存胎动记录", description = "用户记录一次数胎动会话")
    public Result<FetalMovementRecord> saveRecord(@Valid @RequestBody FetalMovementRequest request) {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("保存胎动记录: openId={}, request={}", openId, request);

            FetalMovementRecord record = fetalMovementService.saveRecord(openId, request);
            return Result.success(record);
        } catch (Exception e) {
            log.error("保存胎动记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @GetMapping("/history")
    @Operation(summary = "获取胎动历史", description = "获取用户的胎动记录列表")
    public Result<List<FetalMovementRecord>> getHistory() {
        try {
            String openId = AuthContext.getCurrentOpenId();
            return Result.success(fetalMovementService.getHistory(openId));
        } catch (Exception e) {
            log.error("获取胎动历史失败", e);
            return Result.error(e.getMessage());
        }
    }
}
