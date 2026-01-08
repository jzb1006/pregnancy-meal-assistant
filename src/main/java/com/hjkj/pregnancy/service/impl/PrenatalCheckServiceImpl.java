package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.entity.PrenatalCheckTemplate;
import com.hjkj.pregnancy.entity.UserPrenatalCheck;
import com.hjkj.pregnancy.entity.UserProfile;
import com.hjkj.pregnancy.enums.PregnancyStage;
import com.hjkj.pregnancy.exception.BusinessException;
import com.hjkj.pregnancy.exception.ErrorCode;
import com.hjkj.pregnancy.model.dto.PrenatalCheckToggleRequest;
import com.hjkj.pregnancy.model.vo.NextPrenatalCheckVO;
import com.hjkj.pregnancy.model.vo.PrenatalCheckGroupVO;
import com.hjkj.pregnancy.model.vo.PrenatalCheckItemVO;
import com.hjkj.pregnancy.model.vo.PrenatalCheckTimelineVO;
import com.hjkj.pregnancy.repository.PrenatalCheckTemplateRepository;
import com.hjkj.pregnancy.repository.UserPrenatalCheckRepository;
import com.hjkj.pregnancy.repository.UserProfileRepository;
import com.hjkj.pregnancy.service.PrenatalCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产检时光轴服务实现类
 * 
 * @author Zhibin Jiang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrenatalCheckServiceImpl implements PrenatalCheckService {

    private final PrenatalCheckTemplateRepository templateRepository;
    private final UserPrenatalCheckRepository userCheckRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public PrenatalCheckTimelineVO getTimeline(String openId) {
        log.info("获取产检时光轴: openId={}", openId);

        // 获取用户档案以计算孕周
        UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Integer currentWeek = calculateCurrentWeek(userProfile.getLastMenstrualPeriod());

        // 获取所有启用的产检模板（按孕周时间线排序）
        List<PrenatalCheckTemplate> templates = templateRepository
                .findByIsActiveTrueOrderByWeekRangeStartAscSortOrderAsc();

        // 获取用户的产检状态
        Map<String, UserPrenatalCheck> userCheckMap = userCheckRepository
                .findByOpenId(openId)
                .stream()
                .collect(Collectors.toMap(UserPrenatalCheck::getTemplateCode, check -> check));

        // 按阶段分组
        Map<PregnancyStage, List<PrenatalCheckTemplate>> stageMap = templates.stream()
                .collect(Collectors.groupingBy(PrenatalCheckTemplate::getStage));

        // 构建分组数据
        List<PrenatalCheckGroupVO> groups = Arrays.stream(PregnancyStage.values())
                .filter(stageMap::containsKey)
                .map(stage -> {
                    List<PrenatalCheckTemplate> stageTemplates = stageMap.get(stage);
                    PrenatalCheckTemplate first = stageTemplates.get(0);

                    List<PrenatalCheckItemVO> items = stageTemplates.stream()
                            .map(template -> convertToItemVO(template, userCheckMap.get(template.getCode()), currentWeek))
                            .collect(Collectors.toList());

                    return PrenatalCheckGroupVO.builder()
                            .stage(stage.name())
                            .title(first.getStageTitle())
                            .icon(first.getStageIcon())
                            .items(items)
                            .build();
                })
                .collect(Collectors.toList());

        return PrenatalCheckTimelineVO.builder()
                .currentWeek(currentWeek)
                .groups(groups)
                .build();
    }

    @Override
    @Transactional
    public PrenatalCheckItemVO toggleCheckStatus(String openId, PrenatalCheckToggleRequest request) {
        log.info("切换产检完成状态: openId={}, templateCode={}, done={}", 
                 openId, request.getTemplateCode(), request.getDone());

        // 验证模板是否存在
        PrenatalCheckTemplate template = templateRepository.findByCode(request.getTemplateCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "产检项目不存在"));

        // 校验完成顺序
        validateCheckOrder(openId, request.getTemplateCode(), request.getDone());

        // 查找或创建用户产检状态
        UserPrenatalCheck userCheck = userCheckRepository
                .findByOpenIdAndTemplateCode(openId, request.getTemplateCode())
                .orElse(UserPrenatalCheck.builder()
                        .openId(openId)
                        .templateCode(request.getTemplateCode())
                        .build());

        // 更新状态
        userCheck.setIsDone(request.getDone());
        userCheck.setCheckDate(request.getCheckDate());
        userCheck.setNote(request.getNote());

        UserPrenatalCheck saved = userCheckRepository.save(userCheck);
        log.info("产检状态更新成功: id={}", saved.getId());

        // 获取当前孕周
        UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Integer currentWeek = calculateCurrentWeek(userProfile.getLastMenstrualPeriod());

        return convertToItemVO(template, saved, currentWeek);
    }

    @Override
    public NextPrenatalCheckVO getNextCheck(String openId) {
        log.info("获取下次产检: openId={}", openId);

        // 获取用户档案
        UserProfile userProfile = userProfileRepository.findByOpenId(openId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 获取所有产检模板（按孕周时间线排序）
        List<PrenatalCheckTemplate> templates = templateRepository
                .findByIsActiveTrueOrderByWeekRangeStartAscSortOrderAsc();

        // 获取用户已完成的产检
        Set<String> doneChecks = userCheckRepository.findByOpenId(openId)
                .stream()
                .filter(UserPrenatalCheck::getIsDone)
                .map(UserPrenatalCheck::getTemplateCode)
                .collect(Collectors.toSet());

        // 找到第一个未完成的产检
        for (PrenatalCheckTemplate template : templates) {
            if (!doneChecks.contains(template.getCode())) {
                Integer daysUntil = calculateDaysUntil(
                    userProfile.getLastMenstrualPeriod(), 
                    template.getWeekRangeStart()
                );

                return NextPrenatalCheckVO.builder()
                        .id(template.getCode())
                        .week(formatWeekRange(template.getWeekRangeStart(), template.getWeekRangeEnd()))
                        .title(template.getTitle())
                        .done(false)
                        .daysUntil(daysUntil)
                        .build();
            }
        }

        // 所有产检都已完成
        return NextPrenatalCheckVO.builder()
                .id("all-done")
                .week("40")
                .title("待产")
                .done(false)
                .daysUntil(null)
                .build();
    }

    /**
     * 计算当前孕周
     *
     * @param lmp 末次月经日期
     * @return 当前孕周
     */
    private Integer calculateCurrentWeek(LocalDate lmp) {
        if (lmp == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(lmp, LocalDate.now());
        return (int) (days / 7);
    }

    /**
     * 计算距离建议检查时间还有多少天
     *
     * @param lmp 末次月经日期
     * @param targetWeek 目标孕周
     * @return 剩余天数
     */
    private Integer calculateDaysUntil(LocalDate lmp, Integer targetWeek) {
        if (lmp == null || targetWeek == null) {
            return null;
        }
        LocalDate targetDate = lmp.plusWeeks(targetWeek);
        long days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
        return (int) days;
    }

    /**
     * 格式化孕周范围
     *
     * @param start 开始孕周
     * @param end 结束孕周
     * @return 格式化字符串
     */
    private String formatWeekRange(Integer start, Integer end) {
        if (start.equals(end)) {
            return start.toString();
        }
        return start + "-" + end;
    }

    /**
     * 判断产检项目是否在当前孕周范围内
     *
     * @param currentWeek 当前孕周
     * @param start 开始孕周
     * @param end 结束孕周
     * @return 是否在范围内
     */
    private boolean isInWeekRange(Integer currentWeek, Integer start, Integer end) {
        return currentWeek != null && currentWeek >= start && currentWeek <= end;
    }

    /**
     * 校验产检完成顺序
     * 规则：
     * 1. 标记完成时，前面的项目必须都已完成
     * 2. 取消完成时，后面的项目必须都未完成
     *
     * @param openId 用户标识
     * @param templateCode 产检项目编码
     * @param isDone 是否完成
     */
    private void validateCheckOrder(String openId, String templateCode, Boolean isDone) {
        // 获取所有模板（按孕周时间线顺序）
        List<PrenatalCheckTemplate> allTemplates = templateRepository
                .findByIsActiveTrueOrderByWeekRangeStartAscSortOrderAsc();

        // 找到当前项目的索引
        int currentIndex = -1;
        for (int i = 0; i < allTemplates.size(); i++) {
            if (allTemplates.get(i).getCode().equals(templateCode)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "产检项目不存在");
        }

        // 获取用户已完成的产检
        Set<String> doneChecks = userCheckRepository.findByOpenId(openId)
                .stream()
                .filter(UserPrenatalCheck::getIsDone)
                .map(UserPrenatalCheck::getTemplateCode)
                .collect(Collectors.toSet());

        if (Boolean.TRUE.equals(isDone)) {
            // 标记完成：检查前面的项目是否都已完成
            for (int i = 0; i < currentIndex; i++) {
                String prevCode = allTemplates.get(i).getCode();
                if (!doneChecks.contains(prevCode)) {
                    String prevTitle = allTemplates.get(i).getTitle();
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR, 
                            String.format("请先完成【%s】，再完成此项目", prevTitle));
                }
            }
        } else {
            // 取消完成：检查后面的项目是否都未完成
            for (int i = currentIndex + 1; i < allTemplates.size(); i++) {
                String nextCode = allTemplates.get(i).getCode();
                if (doneChecks.contains(nextCode)) {
                    String nextTitle = allTemplates.get(i).getTitle();
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR, 
                            String.format("请先取消【%s】，再取消此项目", nextTitle));
                }
            }
        }

        log.info("产检顺序校验通过: templateCode={}, isDone={}", templateCode, isDone);
    }

    /**
     * 转换为ItemVO
     *
     * @param template 产检模板
     * @param userCheck 用户产检状态
     * @param currentWeek 当前孕周
     * @return 产检项目VO
     */
    private PrenatalCheckItemVO convertToItemVO(
            PrenatalCheckTemplate template, 
            UserPrenatalCheck userCheck, 
            Integer currentWeek) {
        
        return PrenatalCheckItemVO.builder()
                .id(template.getCode())
                .week(formatWeekRange(template.getWeekRangeStart(), template.getWeekRangeEnd()))
                .title(template.getTitle())
                .shortDesc(template.getShortDesc())
                .details(template.getDetails())
                .tips(template.getTips())
                .done(userCheck != null && userCheck.getIsDone())
                .checkDate(userCheck != null ? userCheck.getCheckDate() : null)
                .isActive(isInWeekRange(currentWeek, template.getWeekRangeStart(), template.getWeekRangeEnd()))
                .build();
    }
}

