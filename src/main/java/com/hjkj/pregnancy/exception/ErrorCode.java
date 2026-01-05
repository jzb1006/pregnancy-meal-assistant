package com.hjkj.pregnancy.exception;

/**
 * 错误码枚举
 * <p>
 * 定义系统中所有的错误码和错误消息
 * </p>
 *
 * @author Zhibin Jiang
 * @since 2025-12-30
 */
public enum ErrorCode {
    // 通用错误 (1000-1999)
    SUCCESS(0, "操作成功"),
    SYSTEM_ERROR(1000, "系统繁忙，请稍后重试"),
    PARAM_ERROR(1001, "参数错误"),
    VALIDATION_ERROR(1002, "数据验证失败"),
    
    // 用户相关错误 (2000-2999)
    USER_NOT_FOUND(2000, "用户不存在，请先完善个人信息"),
    USER_PROFILE_INVALID(2001, "用户档案信息不完整"),
    INVALID_LMP_DATE(2002, "末次月经日期无效"),
    INVALID_BIRTH_DATE(2003, "出生日期无效"),
    INVALID_HEIGHT_WEIGHT(2004, "身高或体重数据无效"),
    
    // AI服务相关错误 (3000-3999)
    AI_SERVICE_ERROR(3000, "AI服务暂时不可用，请稍后重试"),
    AI_TIMEOUT(3001, "AI响应超时"),
    AI_RESPONSE_PARSE_ERROR(3002, "AI响应解析失败"),
    AI_MODEL_NOT_AVAILABLE(3003, "AI模型不可用"),
    
    // 数据相关错误 (4000-4999)
    DATA_NOT_FOUND(4000, "数据不存在"),
    DATA_SAVE_ERROR(4001, "数据保存失败"),
    DATA_UPDATE_ERROR(4002, "数据更新失败"),
    DATA_DELETE_ERROR(4003, "数据删除失败"),
    
    // 业务逻辑错误 (5000-5999)
    INVALID_MEAL_TYPE(5000, "餐次类型无效"),
    RECIPE_NOT_FOUND(5001, "未找到合适的食谱"),
    HISTORY_QUERY_ERROR(5002, "历史记录查询失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }
}



