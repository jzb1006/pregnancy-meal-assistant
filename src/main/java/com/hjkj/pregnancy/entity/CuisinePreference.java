package com.hjkj.pregnancy.entity;

/**
 * 饮食偏好枚举
 * 
 * @author Zhibin Jiang
 */
public enum CuisinePreference {
    
    /**
     * 中餐
     */
    CHINESE("中餐", "中式菜品，以蒸、炒、炖为主，注重营养搭配"),
    
    /**
     * 西餐
     */
    WESTERN("西餐", "西式菜品，以煎、烤、炸为主，讲究精致摆盘"),
    
    /**
     * 日韩料理
     */
    JAPANESE_KOREAN("日韩料理", "日式、韩式菜品，清淡少油，注重食材本味"),
    
    /**
     * 东南亚菜
     */
    SOUTHEAST_ASIAN("东南亚菜", "东南亚风味，香料丰富，酸辣开胃"),
    
    /**
     * 素食
     */
    VEGETARIAN("素食", "素食菜品，以植物性食材为主"),
    
    /**
     * 无偏好
     */
    NO_PREFERENCE("无偏好", "不限菜系，多样化推荐");
    
    private final String label;
    private final String description;
    
    CuisinePreference(String label, String description) {
        this.label = label;
        this.description = description;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getDescription() {
        return description;
    }
}

