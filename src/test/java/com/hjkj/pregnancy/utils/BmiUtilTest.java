package com.hjkj.pregnancy.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BMI工具类测试
 * 
 * @author Zhibin Jiang
 */
class BmiUtilTest {

    @Test
    void testCalculateBmi() {
        // 测试BMI计算
        double bmi = BmiUtil.calculateBmi(165, 58.5);
        assertEquals(21.5, bmi, 0.1);
    }

    @Test
    void testGetBmiCategory() {
        // 测试BMI分类
        assertEquals("UNDERWEIGHT", BmiUtil.getBmiCategory(17.0));
        assertEquals("NORMAL", BmiUtil.getBmiCategory(21.5));
        assertEquals("OVERWEIGHT", BmiUtil.getBmiCategory(26.0));
        assertEquals("OBESE", BmiUtil.getBmiCategory(30.0));
    }

    @Test
    void testGetBmiDescription() {
        // 测试BMI描述
        assertEquals("偏瘦", BmiUtil.getBmiDescription(17.0));
        assertEquals("标准", BmiUtil.getBmiDescription(21.5));
        assertEquals("微胖", BmiUtil.getBmiDescription(26.0));
        assertEquals("肥胖", BmiUtil.getBmiDescription(30.0));
    }

    @Test
    void testGetDietAdvice() {
        // 测试饮食建议
        String advice = BmiUtil.getDietAdvice(21.5);
        assertNotNull(advice);
        assertFalse(advice.isEmpty());
    }

    @Test
    void testFormatBmi() {
        // 测试BMI格式化
        String formatted = BmiUtil.formatBmi(21.567);
        assertEquals("21.6", formatted);
    }

    @Test
    void testInvalidInput() {
        // 测试无效输入
        assertThrows(IllegalArgumentException.class, () -> {
            BmiUtil.calculateBmi(0, 60);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            BmiUtil.calculateBmi(165, 0);
        });
    }
}






