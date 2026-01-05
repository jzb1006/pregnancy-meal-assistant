package com.hjkj.pregnancy.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 饮食偏好枚举测试类
 * 
 * @author Zhibin Jiang
 */
class CuisinePreferenceTest {

    @Test
    void testCuisinePreferenceLabels() {
        assertEquals("中餐", CuisinePreference.CHINESE.getLabel());
        assertEquals("西餐", CuisinePreference.WESTERN.getLabel());
        assertEquals("日韩料理", CuisinePreference.JAPANESE_KOREAN.getLabel());
        assertEquals("东南亚菜", CuisinePreference.SOUTHEAST_ASIAN.getLabel());
        assertEquals("素食", CuisinePreference.VEGETARIAN.getLabel());
        assertEquals("无偏好", CuisinePreference.NO_PREFERENCE.getLabel());
    }

    @Test
    void testCuisinePreferenceDescriptions() {
        assertNotNull(CuisinePreference.CHINESE.getDescription());
        assertNotNull(CuisinePreference.WESTERN.getDescription());
        assertNotNull(CuisinePreference.JAPANESE_KOREAN.getDescription());
        assertNotNull(CuisinePreference.SOUTHEAST_ASIAN.getDescription());
        assertNotNull(CuisinePreference.VEGETARIAN.getDescription());
        assertNotNull(CuisinePreference.NO_PREFERENCE.getDescription());
    }

    @Test
    void testValueOf() {
        assertEquals(CuisinePreference.CHINESE, CuisinePreference.valueOf("CHINESE"));
        assertEquals(CuisinePreference.WESTERN, CuisinePreference.valueOf("WESTERN"));
        assertEquals(CuisinePreference.JAPANESE_KOREAN, CuisinePreference.valueOf("JAPANESE_KOREAN"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            CuisinePreference.valueOf("INVALID");
        });
    }

    @Test
    void testAllPreferences() {
        CuisinePreference[] preferences = CuisinePreference.values();
        assertEquals(6, preferences.length);
    }
}



