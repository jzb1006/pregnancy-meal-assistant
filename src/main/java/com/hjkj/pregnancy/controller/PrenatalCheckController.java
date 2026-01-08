package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.model.dto.PrenatalCheckToggleRequest;
import com.hjkj.pregnancy.model.vo.NextPrenatalCheckVO;
import com.hjkj.pregnancy.model.vo.PrenatalCheckItemVO;
import com.hjkj.pregnancy.model.vo.PrenatalCheckTimelineVO;
import com.hjkj.pregnancy.service.PrenatalCheckService;
import com.hjkj.pregnancy.utils.AuthContext;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 产检时光轴控制器
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@RestController
@RequestMapping("/v1/prenatal-check")
@RequiredArgsConstructor
@Tag(name = "产检管理", description = "产检时光轴相关接口")
public class PrenatalCheckController {

    private final PrenatalCheckService prenatalCheckService;

    @RequireLogin
    @GetMapping("/timeline")
    @Operation(summary = "获取产检时光轴", description = "获取完整的产检时光轴数据，包含所有阶段和用户完成状态")
    public Result<PrenatalCheckTimelineVO> getTimeline() {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("获取产检时光轴: openId={}", openId);
            
            PrenatalCheckTimelineVO timeline = prenatalCheckService.getTimeline(openId);
            return Result.success(timeline);
        } catch (Exception e) {
            log.error("获取产检时光轴失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @PostMapping("/toggle")
    @Operation(summary = "切换完成状态", description = "切换产检项目的完成状态")
    public Result<PrenatalCheckItemVO> toggleStatus(@Valid @RequestBody PrenatalCheckToggleRequest request) {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("切换产检完成状态: openId={}, request={}", openId, request);
            
            PrenatalCheckItemVO item = prenatalCheckService.toggleCheckStatus(openId, request);
            return Result.success(item);
        } catch (Exception e) {
            log.error("切换产检完成状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @GetMapping("/next")
    @Operation(summary = "获取下次产检", description = "获取下一个未完成的产检项目信息")
    public Result<NextPrenatalCheckVO> getNextCheck() {
        try {
            String openId = AuthContext.getCurrentOpenId();
            log.info("获取下次产检: openId={}", openId);
            
            NextPrenatalCheckVO nextCheck = prenatalCheckService.getNextCheck(openId);
            return Result.success(nextCheck);
        } catch (Exception e) {
            log.error("获取下次产检失败", e);
            return Result.error(e.getMessage());
        }
    }
}

