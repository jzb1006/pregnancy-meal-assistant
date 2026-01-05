package com.hjkj.pregnancy.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AgeUtil 工具类单元测试
 *
 * @author Zhibin Jiang
 */
class AgeUtilTest {

    @Test
    void testCalculateAge_Normal() {
        // 假设今天是 2024-12-30
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        int age = AgeUtil.calculateAge(birthDate);
        assertTrue(age >= 34 && age <= 35, "年龄应该在34-35岁之间");
    }

    @Test
    void testCalculateAge_NullBirthDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            AgeUtil.calculateAge(null);
        }, "出生日期为null应该抛出异常");
    }

    @Test
    void testCalculateAge_FutureBirthDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class, () -> {
            AgeUtil.calculateAge(futureDate);
        }, "未来日期应该抛出异常");
    }

    @Test
    void testGetAgeGroup_Young() {
        assertEquals(AgeUtil.AgeGroup.YOUNG, AgeUtil.getAgeGroup(18));
        assertEquals(AgeUtil.AgeGroup.YOUNG, AgeUtil.getAgeGroup(19));
    }

    @Test
    void testGetAgeGroup_Normal() {
        assertEquals(AgeUtil.AgeGroup.NORMAL, AgeUtil.getAgeGroup(20));
        assertEquals(AgeUtil.AgeGroup.NORMAL, AgeUtil.getAgeGroup(28));
        assertEquals(AgeUtil.AgeGroup.NORMAL, AgeUtil.getAgeGroup(34));
    }

    @Test
    void testGetAgeGroup_Senior() {
        assertEquals(AgeUtil.AgeGroup.SENIOR, AgeUtil.getAgeGroup(35));
        assertEquals(AgeUtil.AgeGroup.SENIOR, AgeUtil.getAgeGroup(37));
        assertEquals(AgeUtil.AgeGroup.SENIOR, AgeUtil.getAgeGroup(39));
    }

    @Test
    void testGetAgeGroup_SuperSenior() {
        assertEquals(AgeUtil.AgeGroup.SUPER_SENIOR, AgeUtil.getAgeGroup(40));
        assertEquals(AgeUtil.AgeGroup.SUPER_SENIOR, AgeUtil.getAgeGroup(45));
    }

    @Test
    void testGetAgeGroup_BoundaryValues() {
        // 测试边界值
        assertEquals(AgeUtil.AgeGroup.YOUNG, AgeUtil.getAgeGroup(19));
        assertEquals(AgeUtil.AgeGroup.NORMAL, AgeUtil.getAgeGroup(20));
        assertEquals(AgeUtil.AgeGroup.NORMAL, AgeUtil.getAgeGroup(34));
        assertEquals(AgeUtil.AgeGroup.SENIOR, AgeUtil.getAgeGroup(35));
        assertEquals(AgeUtil.AgeGroup.SENIOR, AgeUtil.getAgeGroup(39));
        assertEquals(AgeUtil.AgeGroup.SUPER_SENIOR, AgeUtil.getAgeGroup(40));
    }

    @Test
    void testGetAgeGroup_InvalidAge() {
        assertThrows(IllegalArgumentException.class, () -> {
            AgeUtil.getAgeGroup(-1);
        }, "负数年龄应该抛出异常");

        assertThrows(IllegalArgumentException.class, () -> {
            AgeUtil.getAgeGroup(101);
        }, "超过100岁应该抛出异常");
    }

    @Test
    void testGetAgeGroupLabel() {
        assertEquals("低龄孕妇", AgeUtil.getAgeGroupLabel(AgeUtil.AgeGroup.YOUNG));
        assertEquals("适龄孕妇", AgeUtil.getAgeGroupLabel(AgeUtil.AgeGroup.NORMAL));
        assertEquals("高龄孕妇", AgeUtil.getAgeGroupLabel(AgeUtil.AgeGroup.SENIOR));
        assertEquals("超高龄孕妇", AgeUtil.getAgeGroupLabel(AgeUtil.AgeGroup.SUPER_SENIOR));
    }

    @Test
    void testGetNutritionAdvice() {
        // 测试每个年龄段都能返回营养建议
        String youngAdvice = AgeUtil.getNutritionAdvice(18);
        assertNotNull(youngAdvice);
        assertTrue(youngAdvice.contains("钙质"), "低龄孕妇建议应包含钙质");

        String normalAdvice = AgeUtil.getNutritionAdvice(28);
        assertNotNull(normalAdvice);
        assertTrue(normalAdvice.contains("均衡"), "适龄孕妇建议应包含均衡");

        String seniorAdvice = AgeUtil.getNutritionAdvice(37);
        assertNotNull(seniorAdvice);
        assertTrue(seniorAdvice.contains("低GI"), "高龄孕妇建议应包含低GI");

        String superSeniorAdvice = AgeUtil.getNutritionAdvice(42);
        assertNotNull(superSeniorAdvice);
        assertTrue(superSeniorAdvice.contains("低盐"), "超高龄孕妇建议应包含低盐");
    }

    @Test
    void testGetDietKeywords() {
        // 测试每个年龄段都能返回饮食关键词
        String youngKeywords = AgeUtil.getDietKeywords(18);
        assertNotNull(youngKeywords);
        assertTrue(youngKeywords.contains("高钙"));

        String normalKeywords = AgeUtil.getDietKeywords(28);
        assertNotNull(normalKeywords);
        assertTrue(normalKeywords.contains("均衡"));

        String seniorKeywords = AgeUtil.getDietKeywords(37);
        assertNotNull(seniorKeywords);
        assertTrue(seniorKeywords.contains("低GI"));

        String superSeniorKeywords = AgeUtil.getDietKeywords(42);
        assertNotNull(superSeniorKeywords);
        assertTrue(superSeniorKeywords.contains("低盐"));
    }

    @Test
    void testIntegration_FullFlow() {
        // 集成测试：完整流程
        LocalDate birthDate = LocalDate.of(1988, 3, 20); // 约36岁
        int age = AgeUtil.calculateAge(birthDate);
        AgeUtil.AgeGroup group = AgeUtil.getAgeGroup(age);
        String label = AgeUtil.getAgeGroupLabel(group);
        String advice = AgeUtil.getNutritionAdvice(age);
        String keywords = AgeUtil.getDietKeywords(age);

        // 验证流程完整性
        assertNotNull(group);
        assertNotNull(label);
        assertNotNull(advice);
        assertNotNull(keywords);
        assertFalse(advice.isEmpty());
        assertFalse(keywords.isEmpty());
    }
}




