package com.hjkj.pregnancy.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.exception.AuthException;
import com.hjkj.pregnancy.exception.ErrorCode;
import com.hjkj.pregnancy.model.vo.LoginResponse;
import com.hjkj.pregnancy.model.vo.UserProfileVO;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.AuthService;
import com.hjkj.pregnancy.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 认证服务实现类
 * <p>
 * 实现微信小程序登录和JWT token生成功能。
 * 登录流程：
 * <ol>
 *   <li>调用微信API，用code换取openId和session_key</li>
 *   <li>根据openId查询用户档案是否存在</li>
 *   <li>生成JWT token（包含openId和userId）</li>
 *   <li>返回登录响应（包含token和用户状态）</li>
 * </ol>
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2026-01-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final WxMaService wxMaService;
    private final JwtUtil jwtUtil;
    private final UserProfileRepository userProfileRepository;

    /**
     * 微信小程序登录实现
     * <p>
     * 处理微信小程序登录流程，包括：
     * <ul>
     *   <li>验证登录凭证code的有效性</li>
     *   <li>调用微信API获取用户openId</li>
     *   <li>检查用户是否已注册档案</li>
     *   <li>生成JWT token</li>
     *   <li>构建登录响应</li>
     * </ul>
     * </p>
     *
     * @param code 微信小程序登录凭证
     * @return 登录响应对象
     * @throws AuthException 如果登录失败（code无效、微信API调用失败等）
     */
    @Override
    public LoginResponse wxLogin(String code) {
        // 1. 参数校验
        if (!StringUtils.hasText(code)) {
            log.warn("登录失败：code为空");
            throw new AuthException(ErrorCode.WX_CODE_INVALID, "登录凭证不能为空");
        }

        try {
            // 2. 调用微信API，code换取session信息
            log.info("开始微信登录，code: {}", code.substring(0, Math.min(10, code.length())) + "...");
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);

            if (session == null || !StringUtils.hasText(session.getOpenid())) {
                log.error("微信登录失败：session为空或openId为空");
                throw new AuthException(ErrorCode.WX_LOGIN_FAILED, "微信登录失败，请重试");
            }

            String openId = session.getOpenid();
            log.info("微信登录成功，获取到openId: {}", openId);

            // 3. 查询或创建用户档案
            UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                    .orElseGet(() -> {
                        // 新用户：自动创建基础用户记录
                        log.info("新用户首次登录，自动创建用户记录: openId={}", openId);
                        UserProfile newUser = UserProfile.builder()
                                .openId(openId)
                                .build();
                        return userProfileRepository.save(newUser);
                    });

            // 4. 判断是否已完善档案
            boolean isNewUser = userProfile.getLastMenstrualPeriod() == null 
                    || userProfile.getHeight() == null 
                    || userProfile.getCurrentWeight() == null
                    || userProfile.getBirthDate() == null;

            // 5. 生成JWT token（包含openId和userId）
            String token = jwtUtil.generateToken(openId, userProfile.getId());
            
            // 6. 构建用户信息（仅已完善档案的用户返回）
            UserProfileVO userInfo = null;
            if (!isNewUser) {
                userInfo = convertToVO(userProfile);
                log.info("老用户登录，openId: {}, userId: {}", openId, userProfile.getId());
            } else {
                log.info("新用户登录（档案未完善），openId: {}, userId: {}", openId, userProfile.getId());
            }

            // 7. 构建响应
            return LoginResponse.builder()
                    .openId(openId)
                    .token(token)
                    .isNewUser(isNewUser)
                    .userInfo(userInfo)
                    .build();

        } catch (WxErrorException e) {
            // 微信API调用异常
            log.error("微信API调用失败，错误码: {}, 错误信息: {}", 
                    e.getError().getErrorCode(), e.getError().getErrorMsg(), e);

            // 根据微信错误码返回友好提示
            if (e.getError().getErrorCode() == 40029) {
                // code无效
                throw new AuthException(ErrorCode.WX_CODE_INVALID, "登录凭证无效或已过期，请重新登录");
            } else if (e.getError().getErrorCode() == 40163) {
                // code已被使用
                throw new AuthException(ErrorCode.WX_CODE_INVALID, "登录凭证已失效，请重新登录");
            } else {
                // 其他微信API错误
                throw new AuthException(ErrorCode.WX_LOGIN_FAILED, "微信登录失败：" + e.getError().getErrorMsg());
            }

        } catch (AuthException e) {
            // 业务异常直接抛出
            throw e;

        } catch (Exception e) {
            // 未知异常
            log.error("登录过程发生异常", e);
            throw new AuthException(ErrorCode.WX_LOGIN_FAILED, "登录失败，请稍后重试");
        }
    }

    /**
     * 将UserProfile实体转换为UserProfileVO
     *
     * @param userProfile 用户档案实体
     * @return 用户档案VO
     */
    private UserProfileVO convertToVO(UserProfile userProfile) {
        // 获取cuisinePreference的字符串表示
        String cuisinePreferenceStr = userProfile.getCuisinePreference() != null 
                ? userProfile.getCuisinePreference().name() 
                : null;
        
        return UserProfileVO.builder()
                .openId(userProfile.getOpenId())
                .lmp(userProfile.getLastMenstrualPeriod())
                .height(userProfile.getHeight())
                .weight(userProfile.getCurrentWeight())
                .birthDate(userProfile.getBirthDate())
                .cuisinePreference(cuisinePreferenceStr)
                .allergies(userProfile.getAllergies())
                .dietaryRestrictions(userProfile.getDietaryRestrictions())
                .preferences(userProfile.getPreferences())
                .build();
    }
}

