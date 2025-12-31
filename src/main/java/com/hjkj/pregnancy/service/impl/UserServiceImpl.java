package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.constants.PregnancyConstants;
import com.hjkj.pregnancy.entity.CuisinePreference;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.exception.UserNotFoundException;
import com.hjkj.pregnancy.model.dto.UserProfileRequest;
import com.hjkj.pregnancy.model.vo.UserProfileVO;
import com.hjkj.pregnancy.model.vo.UserStatusVO;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.UserService;
import com.hjkj.pregnancy.utils.AgeUtil;
import com.hjkj.pregnancy.utils.BmiUtil;
import com.hjkj.pregnancy.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 用户服务实现类
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final Clock clock;

    @Override
    @Transactional
    public UserStatusVO saveOrUpdateProfile(UserProfileRequest request) {
        log.info("保存或更新用户档案: openId={}", request.openId());

        // 查询是否已存在
        UserProfile userProfile = userProfileRepository.findByOpenId(request.openId())
                .orElse(UserProfile.builder()
                        .openId(request.openId())
                        .build());

        // 更新用户信息
        userProfile.setLastMenstrualPeriod(request.lmp());
        userProfile.setHeight(request.height());
        userProfile.setCurrentWeight(request.weight());
        userProfile.setBirthDate(request.birthDate());

        // 更新饮食偏好（可选）
        if (request.cuisinePreference() != null && !request.cuisinePreference().isBlank()) {
            try {
                CuisinePreference preference = CuisinePreference.valueOf(request.cuisinePreference().toUpperCase());
                userProfile.setCuisinePreference(preference);
            } catch (IllegalArgumentException e) {
                log.warn("无效的饮食偏好: {}", request.cuisinePreference());
                // 如果传入无效值，不更新此字段
            }
        }

        // 更新精细化档案信息
        if (request.allergies() != null) {
            userProfile.setAllergies(request.allergies());
        }
        if (request.dietaryRestrictions() != null) {
            userProfile.setDietaryRestrictions(request.dietaryRestrictions());
        }
        if (request.preferences() != null) {
            userProfile.setPreferences(request.preferences());
        }

        // 保存到数据库
        userProfileRepository.save(userProfile);

        // 计算并返回当前状态
        return buildUserStatus(userProfile);
    }

    @Override
    public UserStatusVO getUserStatus(String openId) {
        log.info("获取用户状态: openId={}", openId);

        UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new UserNotFoundException(openId));

        return buildUserStatus(userProfile);
    }

    @Override
    public UserProfileVO getUserProfile(String openId) {
        log.info("获取用户档案: openId={}", openId);

        UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new UserNotFoundException(openId));

        return UserProfileVO.builder()
                .openId(userProfile.getOpenId())
                .lmp(userProfile.getLastMenstrualPeriod())
                .height(userProfile.getHeight())
                .weight(userProfile.getCurrentWeight())
                .birthDate(userProfile.getBirthDate())
                .cuisinePreference(
                        userProfile.getCuisinePreference() != null ? userProfile.getCuisinePreference().name() : null)
                .cuisinePreferenceLabel(
                        userProfile.getCuisinePreference() != null ? userProfile.getCuisinePreference().getLabel()
                                : null)
                .allergies(userProfile.getAllergies())
                .dietaryRestrictions(userProfile.getDietaryRestrictions())
                .preferences(userProfile.getPreferences())
                .build();
    }

    /**
     * 构建用户状态信息
     * <p>
     * 使用注入的 Clock 实例统一时间来源，确保时区一致性。
     * 计算孕期天数（pregnancyDays）用于精确判断是否超预产期。
     * </p>
     * <p>
     * 特殊处理：
     * <ul>
     *   <li>当 days < 0 时（未来 LMP），将 days 和 week 设为 0，避免负值异常</li>
     *   <li>使用 PregnancyConstants 中定义的常量，避免魔法数字</li>
     * </ul>
     * </p>
     *
     * @param userProfile 用户档案实体
     * @return 用户状态信息
     */
    private UserStatusVO buildUserStatus(UserProfile userProfile) {
        // 使用统一的 Clock 获取今天的日期
        LocalDate today = LocalDate.now(clock);

        // 计算从末次月经到今天的累计天数
        long days = ChronoUnit.DAYS.between(userProfile.getLastMenstrualPeriod(), today);

        // 处理负数情况（未来 LMP），避免异常
        if (days < 0) {
            days = 0;
        }

        // 计算孕周（天数除以7），使用常量避免魔法数字
        int week = (int) (days / PregnancyConstants.PregnancyStage.DAYS_PER_WEEK);

        // 计算BMI
        double bmi = BmiUtil.calculateBmi(userProfile.getHeight(), userProfile.getCurrentWeight());
        String bmiDesc = BmiUtil.getBmiDescription(bmi);

        // 计算年龄
        int age = AgeUtil.calculateAge(userProfile.getBirthDate());

        // 获取孕期阶段
        String stage = DateUtil.getPregnancyStage(week);

        // 生成温馨提示
        String babyDesc = DateUtil.getBabyDescription(week);
        String dietAdvice = BmiUtil.getDietAdvice(bmi);
        String tips = String.format("%s，%s", babyDesc, dietAdvice);

        return UserStatusVO.builder()
                .week(week)
                .bmi(Double.parseDouble(BmiUtil.formatBmi(bmi)))
                .bmiDesc(bmiDesc)
                .stage(stage)
                .age(age)
                .tips(tips)
                .pregnancyDays((int) days)  // 新增：累计天数用于超期判断
                .build();
    }
}
