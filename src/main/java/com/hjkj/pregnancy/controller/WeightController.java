package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.model.dto.WeightRecordRequest;
import com.hjkj.pregnancy.model.vo.PrePregnancyWeightVO;
import com.hjkj.pregnancy.model.vo.WeightRecordVO;
import com.hjkj.pregnancy.model.vo.WeightStatsVO;
import com.hjkj.pregnancy.service.WeightService;
import com.hjkj.pregnancy.utils.AuthContext;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 体重管理控制器
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@RestController
@RequestMapping("/v1/weight")
@RequiredArgsConstructor
@Tag(name = "体重管理", description = "孕期体重记录与统计相关接口")
public class WeightController {

    private final WeightService weightService;

    @RequireLogin
    @PostMapping("/record")
    @Operation(summary = "保存体重记录", description = "记录孕期体重，同一天的记录会被更新")
    public Result<WeightRecordVO> saveRecord(@Valid @RequestBody WeightRecordRequest request) {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("保存体重记录: openId={}, request={}", openId, request);
            
            WeightRecordVO result = weightService.saveWeightRecord(openId, request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("保存体重记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @GetMapping("/history")
    @Operation(summary = "获取体重历史记录", description = "查询用户的体重历史记录")
    public Result<List<WeightRecordVO>> getHistory(
            @Parameter(description = "开始日期") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            
            @Parameter(description = "结束日期") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            
            @Parameter(description = "返回条数限制") 
            @RequestParam(required = false) Integer limit) {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("查询体重历史: openId={}, startDate={}, endDate={}, limit={}", 
                     openId, startDate, endDate, limit);
            
            List<WeightRecordVO> records = weightService.getWeightHistory(openId, startDate, endDate, limit);
            return Result.success(records);
        } catch (Exception e) {
            log.error("查询体重历史失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @GetMapping("/pre-pregnancy")
    @Operation(summary = "获取孕前体重", description = "从用户档案获取孕前体重")
    public Result<PrePregnancyWeightVO> getPrePregnancyWeight() {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("获取孕前体重: openId={}", openId);
            
            PrePregnancyWeightVO result = weightService.getPrePregnancyWeight(openId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取孕前体重失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @GetMapping("/stats")
    @Operation(summary = "获取体重统计", description = "获取当前体重、增重量、BMI等统计信息")
    public Result<WeightStatsVO> getStats() {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("获取体重统计: openId={}", openId);
            
            WeightStatsVO stats = weightService.getWeightStats(openId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取体重统计失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @DeleteMapping("/record/{id}")
    @Operation(summary = "删除体重记录", description = "删除指定的体重记录")
    public Result<String> deleteRecord(
            @Parameter(description = "记录ID", required = true) 
            @PathVariable Long id) {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("删除体重记录: openId={}, recordId={}", openId, id);
            
            weightService.deleteWeightRecord(openId, id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除体重记录失败", e);
            return Result.error(e.getMessage());
        }
    }
}

