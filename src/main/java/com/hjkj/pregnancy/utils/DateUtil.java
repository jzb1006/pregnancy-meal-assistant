package com.hjkj.pregnancy.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 * 
 * @author Zhibin Jiang
 */
public class DateUtil {

    /**
     * 根据末次月经计算当前孕周
     * 
     * @param lmp 末次月经日期
     * @return 孕周数
     */
    public static int calculatePregnancyWeek(LocalDate lmp) {
        if (lmp == null) {
            throw new IllegalArgumentException("末次月经日期不能为空");
        }
        
        LocalDate now = LocalDate.now();
        long days = ChronoUnit.DAYS.between(lmp, now);
        
        if (days < 0) {
            throw new IllegalArgumentException("末次月经日期不能是未来时间");
        }
        
        return (int) (days / 7);
    }

    /**
     * 计算预产期
     * 
     * @param lmp 末次月经日期
     * @return 预产期
     */
    public static LocalDate calculateDueDate(LocalDate lmp) {
        if (lmp == null) {
            throw new IllegalArgumentException("末次月经日期不能为空");
        }
        
        // 预产期 = 末次月经 + 280天
        return lmp.plusDays(280);
    }

    /**
     * 根据孕周判断孕期阶段
     * 
     * @param week 孕周
     * @return 孕期阶段描述
     */
    public static String getPregnancyStage(int week) {
        if (week < 0) {
            throw new IllegalArgumentException("孕周不能为负数");
        }
        
        if (week <= 12) {
            return "孕早期";
        } else if (week <= 28) {
            return "孕中期";
        } else if (week <= 40) {
            return "孕晚期";
        } else {
            return "已过预产期";
        }
    }

    /**
     * 获取孕周对应的胎儿发育描述
     * 
     * @param week 孕周
     * @return 发育描述
     */
    public static String getBabyDescription(int week) {
        return switch (week) {
            case 1, 2, 3, 4 -> "宝宝现在还是一个小小的胚胎";
            case 5, 6, 7, 8 -> "宝宝像一颗小豆子，心脏开始跳动";
            case 9, 10, 11, 12 -> "宝宝现在像个柠檬，器官开始发育";
            case 13, 14, 15, 16 -> "宝宝像个苹果，开始长头发啦";
            case 17, 18, 19, 20 -> "宝宝像个小番茄，可以感知外界声音";
            case 21, 22, 23, 24 -> "宝宝像个小木瓜，开始有规律的睡眠";
            case 25, 26, 27, 28 -> "宝宝像个大茄子，可以睁开眼睛了";
            case 29, 30, 31, 32 -> "宝宝像个大椰子，皮肤开始变光滑";
            case 33, 34, 35, 36 -> "宝宝像个大菠萝，肺部接近成熟";
            case 37, 38, 39, 40 -> "宝宝像个小西瓜，随时准备和你见面";
            default -> week > 40 ? "宝宝已经足月，期待与你相见" : "宝宝正在健康成长";
        };
    }
}


