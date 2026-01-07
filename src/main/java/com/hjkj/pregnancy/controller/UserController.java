package com.hjkj.pregnancy.controller;

import com.hjkj.pregnancy.annotation.RequireLogin;
import com.hjkj.pregnancy.enums.MoodType;
import com.hjkj.pregnancy.model.dto.UserProfileRequest;
import com.hjkj.pregnancy.model.vo.DailyEncouragementVO;
import com.hjkj.pregnancy.model.vo.UserProfileVO;
import com.hjkj.pregnancy.model.vo.UserStatusVO;
import com.hjkj.pregnancy.service.DailyEncouragementService;
import com.hjkj.pregnancy.service.UserService;
import com.hjkj.pregnancy.utils.AuthContext;
import com.hjkj.pregnancy.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Zhibin Jiang
 */
@Slf4j
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户档案相关接口")
public class UserController {

    private final UserService userService;
    private final DailyEncouragementService dailyEncouragementService;

    @RequireLogin
    @PostMapping("/profile")
    @Operation(summary = "初始化/更新用户档案", description = "用户首次登录或更新体重时调用，需要登录token。openId从token中自动获取，无需传递。")
    public Result<UserStatusVO> saveOrUpdateProfile(@Valid @RequestBody UserProfileRequest request) {
        try {
            // 从token中获取当前用户openId
            String openId = AuthContext.getCurrentOpenId();
            log.info("收到用户档案请求: openId={}", openId);
            
            UserStatusVO status = userService.saveOrUpdateProfile(openId, request);
            return Result.success(status);
        } catch (Exception e) {
            log.error("保存用户档案失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @GetMapping("/status")
    @Operation(summary = "获取今日状态", description = "打开App首页时调用，获取当前孕周和建议，需要登录token")
    public Result<UserStatusVO> getUserStatus() {
        try {
            // 从token中获取当前用户openId
            String openId = AuthContext.getCurrentOpenId();
            log.info("查询用户状态: {}", openId);
            UserStatusVO status = userService.getUserStatus(openId);
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取用户状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @GetMapping("/info")
    @Operation(summary = "获取用户完整档案", description = "获取用户完整档案信息，用于信息回显，需要登录token")
    public Result<UserProfileVO> getProfile() {
        try {
            // 从token中获取当前用户openId
            String openId = AuthContext.getCurrentOpenId();
            log.info("查询用户档案: {}", openId);
            UserProfileVO profile = userService.getUserProfile(openId);
            return Result.success(profile);
        } catch (Exception e) {
            log.error("获取用户档案失败", e);
            return Result.error(e.getMessage());
        }
    }

    @RequireLogin
    @PostMapping("/daily-encouragement")
    @Operation(summary = "获取每日鼓励语录", description = "获取宝宝视角的每日暖心鼓励，同一天内幂等返回，需要登录token")
    public Result<DailyEncouragementVO> getDailyEncouragement(
            @Parameter(description = "当前心情（可选，默认HAPPY）", required = false) @RequestParam(required = false) MoodType mood) {
        try {
            // 从token中获取当前用户openId
            String openId = AuthContext.getCurrentOpenId();
            log.info("收到每日鼓励请求: openId={}, mood={}", openId, mood);
            DailyEncouragementVO encouragement = dailyEncouragementService.getDailyEncouragement(openId, mood);
            return Result.success(encouragement);
        } catch (Exception e) {
            log.error("获取每日鼓励失败", e);
            return Result.error(e.getMessage());
        }
    }
}
