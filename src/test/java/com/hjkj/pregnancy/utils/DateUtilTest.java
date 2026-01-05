package com.hjkj.pregnancy.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 日期工具类测试
 * 
 * @author Zhibin Jiang
 */
class DateUtilTest {

    @Test
    void testCalculatePregnancyWeek() {
        // 测试当前孕周计算
        LocalDate lmp = LocalDate.now().minusDays(84); // 12周前
        int week = DateUtil.calculatePregnancyWeek(lmp);
        assertEquals(12, week);
    }

    @Test
    void testCalculateDueDate() {
        // 测试预产期计算
        LocalDate lmp = LocalDate.of(2025, 1, 1);
        LocalDate dueDate = DateUtil.calculateDueDate(lmp);
        LocalDate expected = lmp.plusDays(280);
        assertEquals(expected, dueDate);
    }

    @Test
    void testGetPregnancyStage() {
        // 测试孕期阶段
        assertEquals("孕早期", DateUtil.getPregnancyStage(10));
        assertEquals("孕中期", DateUtil.getPregnancyStage(20));
        assertEquals("孕晚期", DateUtil.getPregnancyStage(35));
    }

    @Test
    void testGetBabyDescription() {
        // 测试胎儿发育描述
        String description = DateUtil.getBabyDescription(12);
        assertNotNull(description);
        assertFalse(description.isEmpty());
    }

    @Test
    void testInvalidInput() {
        // 测试无效输入
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtil.calculatePregnancyWeek(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtil.calculatePregnancyWeek(LocalDate.now().plusDays(1));
        });
    }
}




