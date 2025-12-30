package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.entity.CuisinePreference;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.exception.UserNotFoundException;
import com.hjkj.pregnancy.model.dto.UserProfileRequest;
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

    /**
     * 构建用户状态信息
     */
    private UserStatusVO buildUserStatus(UserProfile userProfile) {
        // 计算孕周
        int week = DateUtil.calculatePregnancyWeek(userProfile.getLastMenstrualPeriod());

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
                .build();
    }
}
